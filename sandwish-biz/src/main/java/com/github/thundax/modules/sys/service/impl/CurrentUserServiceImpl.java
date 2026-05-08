package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.utils.PasswordHelper;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserCredential;
import com.github.thundax.modules.sys.service.CurrentUserService;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserCredentialService;
import com.github.thundax.modules.sys.service.UserIdentityService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import com.github.thundax.modules.sys.utils.SysApiUtils;
import java.util.List;
import java.util.Objects;
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
    private final UserCredentialService userCredentialService;
    private final UserIdentityService userIdentityService;

    public CurrentUserServiceImpl(
            UserService userService,
            RoleService roleService,
            MenuService menuService,
            UserCredentialService userCredentialService,
            UserIdentityService userIdentityService) {
        this.userService = userService;
        this.roleService = roleService;
        this.menuService = menuService;
        this.userCredentialService = userCredentialService;
        this.userIdentityService = userIdentityService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateInfo(User currentUser, String name, String email, String mobile) {
        currentUser.setName(name);
        currentUser.setEmail(email);
        currentUser.setMobile(mobile);
        userService.update(currentUser, userIdentityService.getAccountLoginName(currentUser.getId()), null);
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

        UserCredential credential = userCredentialService.getPasswordCredential(currentUser.getId());
        if (credential == null || !PasswordHelper.validate(oldPassword, credential.getCredentialValue())) {
            throw new InvalidPasswordException();
        }

        userCredentialService.upsertPassword(currentUser, PasswordHelper.encrypt(password));
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

        List<EntityId> menuIds = roleList.stream()
                .flatMap(role -> roleService.listRoleMenus(role).stream())
                .map(Menu::getId)
                .distinct()
                .filter(menuId -> {
                    Menu menu = menuService.getById(menuId);
                    return menu != null && currentUser.getRank().canAccess(menu.getRank());
                })
                .collect(Collectors.toList());
        List<Menu> menuList = menuService.listByIds(menuIds);
        menuList.sort(Menu::compareTo);
        return menuList;
    }

    @Override
    public List<Menu> listVisibleMenus(User currentUser) {
        List<Menu> visibleMenus = listAccessibleMenus(currentUser).stream()
                .filter(Menu::isDisplay)
                .collect(Collectors.toList());
        List<Menu> menuList =
                visibleMenus.stream().filter(menu -> menu.getParentId() == null).collect(Collectors.toList());

        for (int idx = 0; idx < menuList.size(); idx++) {
            Menu parent = menuList.get(idx);
            List<Menu> childList = visibleMenus.stream()
                    .filter(menu -> Objects.equals(menu.getParentId(), parent.getId()))
                    .collect(Collectors.toList());
            menuList.addAll(childList);
        }
        return menuList;
    }
}
