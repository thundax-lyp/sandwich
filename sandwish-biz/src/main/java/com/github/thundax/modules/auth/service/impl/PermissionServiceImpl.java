package com.github.thundax.modules.auth.service.impl;

import static com.github.thundax.modules.sys.entity.valueobject.PermissionCode.ADMIN;
import static com.github.thundax.modules.sys.entity.valueobject.PermissionCode.SEPARATOR;
import static com.github.thundax.modules.sys.entity.valueobject.PermissionCode.SUPER;
import static com.github.thundax.modules.sys.entity.valueobject.PermissionCode.USER;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.security.permission.PermissionMatcher;
import com.github.thundax.common.security.permission.PrefixPermissionMatcher;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.PermissionDao;
import com.github.thundax.modules.auth.entity.PermissionSession;
import com.github.thundax.modules.auth.service.PermissionService;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.valueobject.PermissionCode;
import com.github.thundax.modules.sys.service.CurrentUserService;
import com.github.thundax.modules.sys.service.UserService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    private final CurrentUserService currentUserService;
    private final PermissionMatcher permissionMatcher = new PrefixPermissionMatcher();

    public PermissionServiceImpl(
            PermissionDao permissionDao,
            AuthProperties properties,
            UserService userService,
            CurrentUserService currentUserService) {
        this.permissionDao = permissionDao;
        this.properties = properties;
        this.userService = userService;
        this.currentUserService = currentUserService;
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
        User user = userService.getById(EntityIdCodec.toDomain(Long.valueOf(userId)));
        Assert.notNull(user, "user can not be null");

        Set<String> permissions = new HashSet<>();
        List<Menu> menuList = currentUserService.listAccessibleMenus(user);
        if (menuList != null && !menuList.isEmpty()) {
            menuList.forEach(menu -> {
                if (StringUtils.isNotBlank(menu.getPerms())) {
                    for (String permission : StringUtils.split(menu.getPerms(), SEPARATOR)) {
                        if (!PermissionCode.isBuiltIn(permission)) {
                            permissions.add(permission);
                        }
                    }
                }
            });
        }

        permissions.add(USER);
        if (user.isSuper()) {
            permissions.add(SUPER);
            permissions.add(ADMIN);
        } else if (user.isAdmin()) {
            permissions.add(ADMIN);
        }

        return permissions;
    }

    private int expiredSeconds() {
        return properties.getLoginExpiredSeconds() + SAFETY_SECONDS;
    }
}
