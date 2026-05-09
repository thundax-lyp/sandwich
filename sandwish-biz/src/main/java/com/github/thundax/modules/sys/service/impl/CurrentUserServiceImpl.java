package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.utils.PasswordHelper;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.CurrentUserService;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.command.ChangeUserInfoCommand;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import com.github.thundax.modules.sys.service.query.UserQuery;
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

    private static final int DEFAULT_PASSWORD_FAILED_LIMIT = 0;

    private final UserService userService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final PrincipalIdentityService principalIdentityService;
    private final PrincipalCredentialService principalCredentialService;

    public CurrentUserServiceImpl(
            UserService userService,
            RoleService roleService,
            MenuService menuService,
            PrincipalIdentityService principalIdentityService,
            PrincipalCredentialService principalCredentialService) {
        this.userService = userService;
        this.roleService = roleService;
        this.menuService = menuService;
        this.principalIdentityService = principalIdentityService;
        this.principalCredentialService = principalCredentialService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateInfo(User currentUser, String name, String email, String mobile) {
        currentUser.setName(name);
        currentUser.setEmail(email);
        currentUser.setMobile(mobile);
        userService.changeInfo(new ChangeUserInfoCommand(
                currentUser.getId(),
                currentUser.getDepartmentId(),
                currentUser.getEmail(),
                currentUser.getMobile(),
                currentUser.getTel(),
                currentUser.getName(),
                currentUser.getRank(),
                currentUser.getPrivilege(),
                currentUser.getStatus(),
                currentUser.getPriority(),
                currentUser.getRemarks(),
                getAccountLoginName(currentUser.getId()),
                null));
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

        PrincipalIdentity accountIdentity = getAccountIdentity(currentUser.getId());
        PrincipalCredential credential = accountIdentity == null
                ? null
                : principalCredentialService.getByIdentityIdAndType(
                        accountIdentity.getId(), PrincipalCredentialType.USER_PASSWORD);
        if (credential == null || !PasswordHelper.validate(oldPassword, credential.getCredentialValue())) {
            throw new InvalidPasswordException();
        }

        upsertPassword(currentUser, accountIdentity, PasswordHelper.encrypt(password));
    }

    @Override
    public List<Menu> listAccessibleMenus(User currentUser) {
        if (currentUser.isSuper()) {
            List<Menu> menuList = menuService.list(new MenuQuery());
            menuList.sort(Menu::compareTo);
            return menuList;
        }

        List<Role> roleList = userService.listUserRoles(userQuery(currentUser));
        boolean isAdmin = currentUser.isAdmin() || roleList.stream().anyMatch(Role::isAdmin);
        if (isAdmin) {
            List<Menu> menuList = menuService.list(new MenuQuery(null, null, currentUser.getRank()));
            menuList.sort(Menu::compareTo);
            return menuList;
        }

        List<EntityId> menuIds = roleList.stream()
                .flatMap(role -> roleService.listRoleMenus(roleQuery(role)).stream())
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

    private RoleQuery roleQuery(Role role) {
        RoleQuery query = new RoleQuery();
        query.setId(role.getId());
        return query;
    }

    private UserQuery userQuery(User user) {
        UserQuery query = new UserQuery();
        query.setId(user.getId());
        return query;
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

    private PrincipalIdentity getAccountIdentity(EntityId userId) {
        if (userId == null) {
            return null;
        }
        return principalIdentityService.getByPrincipalKeyAndType(
                PrincipalKey.of(PrincipalType.USER, userId), PrincipalIdentityType.USER_ACCOUNT);
    }

    private String getAccountLoginName(EntityId userId) {
        PrincipalIdentity identity = getAccountIdentity(userId);
        return identity == null ? null : identity.getIdentityValue();
    }

    private void upsertPassword(User user, PrincipalIdentity accountIdentity, String encryptedPassword) {
        if (user == null || accountIdentity == null || StringUtils.isBlank(encryptedPassword)) {
            return;
        }
        PrincipalCredential credential = principalCredentialService.getByIdentityIdAndType(
                accountIdentity.getId(), PrincipalCredentialType.USER_PASSWORD);
        if (credential == null) {
            credential = new PrincipalCredential();
            credential.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, user.getId()));
            credential.setIdentityId(accountIdentity.getId());
            credential.setCredentialType(PrincipalCredentialType.USER_PASSWORD);
            credential.setCredentialValue(encryptedPassword);
            credential.setStatus(PrincipalCredentialStatus.ACTIVE);
            credential.setNeedChangePassword(false);
            credential.setFailedCount(0);
            credential.setFailedLimit(DEFAULT_PASSWORD_FAILED_LIMIT);
            principalCredentialService.add(credential);
            return;
        }

        credential.setCredentialValue(encryptedPassword);
        credential.setStatus(PrincipalCredentialStatus.ACTIVE);
        credential.setNeedChangePassword(false);
        credential.setFailedCount(0);
        credential.setLockedUntil(null);
        credential.setLastVerifiedAt(null);
        principalCredentialService.update(credential);
    }
}
