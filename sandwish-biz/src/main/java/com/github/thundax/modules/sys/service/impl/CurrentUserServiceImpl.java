package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserCredential;
import com.github.thundax.modules.sys.service.CurrentUserService;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import com.github.thundax.modules.sys.utils.SysApiUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CurrentUserServiceImpl implements CurrentUserService {

    private final UserService userService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final PasswordService passwordService;

    public CurrentUserServiceImpl(
            UserService userService,
            RoleService roleService,
            MenuService menuService,
            PasswordService passwordService) {
        this.userService = userService;
        this.roleService = roleService;
        this.menuService = menuService;
        this.passwordService = passwordService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateInfo(User currentUser, String name, String email, String mobile) {
        currentUser.setName(name);
        currentUser.setEmail(email);
        currentUser.setMobile(mobile);
        userService.update(currentUser, userService.getAccountLoginName(currentUser.getId()), null);
        return currentUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(User currentUser, String oldPassword, String password) throws ApiException {
        if (StringUtils.isBlank(password)) {
            throw new InvalidParameterException("password");
        } else if (!password.matches(SysApiUtils.PASSWORD_VALIDATE_PATTERN)) {
            throw new ApiException(SysApiUtils.PASSWORD_VALIDATE_MESSAGE);
        }

        UserCredential credential = userService.getPasswordCredential(currentUser.getId());
        if (credential == null || !passwordService.validate(oldPassword, credential.getCredentialValue())) {
            throw new InvalidPasswordException();
        }

        userService.updatePassword(
                currentUser.getId(), passwordService.encrypt(password), EntityIdCodec.toValue(currentUser.getId()));
    }

    @Override
    public List<Menu> listAccessibleMenus(User currentUser) {
        if (currentUser.isSuper()) {
            List<Menu> menuList = menuService.list(new MenuQuery());
            menuList.sort(Menu::compareTo);
            return menuList;
        }

        List<Role> roleList = userService.listUserRoles(currentUser);
        boolean isAdmin = currentUser.isAdmin() || roleList.stream().anyMatch(Role::isAdmin);
        if (isAdmin) {
            List<Menu> menuList = menuService.list(new MenuQuery(null, null, currentUser.getRank()));
            menuList.sort(Menu::compareTo);
            return menuList;
        }

        Set<String> menuIds = Sets.newHashSet();
        for (Role role : roleList) {
            menuIds.addAll(roleService.listRoleMenus(role).stream()
                    .map(menu -> EntityIdCodec.toValue(menu.getId()))
                    .collect(Collectors.toList()));
        }
        menuIds.removeIf(menuId -> {
            Menu menu = menuService.getById(EntityIdCodec.toDomain(menuId));
            return menu == null || !currentUser.getRank().canAccess(menu.getRank());
        });
        List<Menu> menuList = menuService.listByIds(EntityIdCodec.toDomains(new ArrayList<>(menuIds)));
        menuList.sort(Menu::compareTo);
        return menuList;
    }

    @Override
    public List<Menu> listVisibleMenus(User currentUser) {
        List<Menu> allMenuList = listAccessibleMenus(currentUser);
        Menu rootMenu = new Menu();
        List<Menu> menuList = Lists.newArrayList(rootMenu);

        for (int idx = 0; idx < menuList.size(); idx++) {
            Menu parent = menuList.get(idx);
            List<Menu> childList = allMenuList == null
                    ? new ArrayList<>()
                    : allMenuList.stream()
                            .filter(menu -> menu.isDisplay() && StringUtils.equals(menu.getParentId(), menuId(parent)))
                            .collect(Collectors.toList());
            menuList.addAll(idx + 1, childList);
        }

        menuList.remove(0);
        return menuList;
    }

    private String menuId(Menu menu) {
        return menu == null ? null : EntityIdCodec.toValue(menu.getId());
    }
}
