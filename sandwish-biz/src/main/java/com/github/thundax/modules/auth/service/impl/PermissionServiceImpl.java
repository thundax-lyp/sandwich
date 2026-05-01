package com.github.thundax.modules.auth.service.impl;

import static com.github.thundax.modules.sys.entity.Menu.PERM_ADMIN;
import static com.github.thundax.modules.sys.entity.Menu.PERM_SEPARATOR;
import static com.github.thundax.modules.sys.entity.Menu.PERM_SUPER;
import static com.github.thundax.modules.sys.entity.Menu.PERM_USER;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.security.permission.PermissionMatcher;
import com.github.thundax.common.security.permission.PrefixPermissionMatcher;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.PermissionDao;
import com.github.thundax.modules.auth.entity.PermissionSession;
import com.github.thundax.modules.auth.service.PermissionService;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@EnableConfigurationProperties(AuthProperties.class)
public class PermissionServiceImpl implements PermissionService {

    private static final int SAFETY_SECONDS = 10;

    private final PermissionDao permissionDao;
    private final AuthProperties properties;
    private final UserService userService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final PermissionMatcher permissionMatcher = new PrefixPermissionMatcher();

    public PermissionServiceImpl(
            PermissionDao permissionDao,
            AuthProperties properties,
            UserService userService,
            RoleService roleService,
            MenuService menuService) {
        this.permissionDao = permissionDao;
        this.properties = properties;
        this.userService = userService;
        this.roleService = roleService;
        this.menuService = menuService;
    }

    @Override
    public PermissionSession createSession(String token, String userId) {
        Assert.hasText(token, "token can not be empty");
        Assert.hasText(userId, "userId can not be empty");

        PermissionSession session = new PermissionSession();
        session.setToken(token);
        session.setUserId(userId);
        session.setPermissions(loadPermissions(userId));
        session.setVersion(UUID.randomUUID().toString());
        session.setTimestamp(System.currentTimeMillis());

        permissionDao.insert(session, expiredSeconds());

        return session;
    }

    @Override
    public PermissionSession getSession(String token) {
        PermissionSession session = permissionDao.getByToken(token);
        if (session != null) {
            touch(token);
        }
        return session;
    }

    @Override
    public void touch(String token) {
        if (StringUtils.isNotBlank(token)) {
            permissionDao.touch(token, expiredSeconds());
        }
    }

    @Override
    public void release(String token) {
        if (StringUtils.isNotBlank(token)) {
            permissionDao.deleteByToken(token);
        }
    }

    @Override
    public void reloadAll() {
        permissionDao.deleteAll();
    }

    @Override
    public boolean isPermitted(String token, String permission) {
        PermissionSession session = getSession(token);
        return session != null && permissionMatcher.matches(session.getPermissions(), permission);
    }

    private Set<String> loadPermissions(String userId) {
        User user = userService.getById(EntityIdCodec.toDomain(userId));
        Assert.notNull(user, "user can not be null");

        Set<String> permissions = new HashSet<>();
        List<Menu> menuList = listPermittedMenus(user);
        if (menuList != null && !menuList.isEmpty()) {
            menuList.forEach(menu -> {
                if (StringUtils.isNotBlank(menu.getPerms())) {
                    for (String permission : StringUtils.split(menu.getPerms(), PERM_SEPARATOR)) {
                        if (!PERM_USER.equals(permission)
                                && !PERM_SUPER.equals(permission)
                                && !PERM_ADMIN.equals(permission)) {
                            permissions.add(permission);
                        }
                    }
                }
            });
        }

        permissions.add(PERM_USER);
        if (user.isSuper()) {
            permissions.add(PERM_SUPER);
            permissions.add(PERM_ADMIN);
        } else if (user.isAdmin()) {
            permissions.add(PERM_ADMIN);
        }

        return permissions;
    }

    private List<Menu> listPermittedMenus(User user) {
        List<String> menuIdList;

        if (user.isSuper()) {
            menuIdList = menuService.list(new Menu()).stream()
                    .map(menu -> EntityIdCodec.toValue(menu.getId()))
                    .collect(Collectors.toList());
        } else {
            List<Role> roleList = userService.listUserRoles(user);
            boolean isAdmin = user.isAdmin() || roleList.stream().anyMatch(Role::isAdmin);

            if (isAdmin) {
                menuIdList = menuService.list(user.getRanks()).stream()
                        .map(menu -> EntityIdCodec.toValue(menu.getId()))
                        .collect(Collectors.toList());
            } else {
                Set<String> menuIds = Sets.newHashSet();
                for (Role role : roleList) {
                    menuIds.addAll(roleService.listRoleMenus(role).stream()
                            .map(menu -> EntityIdCodec.toValue(menu.getId()))
                            .collect(Collectors.toList()));
                }
                menuIds.removeIf(menuId -> {
                    Menu menu = menuService.getById(EntityIdCodec.toDomain(menuId));
                    return menu == null || menu.getRanks() > user.getRanks();
                });
                menuIdList = new ArrayList<>(menuIds);
            }
        }

        List<Menu> menuList = menuService.batchGetByIds(menuIdList);
        menuList.sort(Menu::compareTo);
        return menuList;
    }

    private int expiredSeconds() {
        return properties.getLoginExpiredSeconds() + SAFETY_SECONDS;
    }
}
