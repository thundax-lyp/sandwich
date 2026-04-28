package com.github.thundax.modules.auth.service.impl;

import static com.github.thundax.modules.sys.entity.Menu.PERM_ADMIN;
import static com.github.thundax.modules.sys.entity.Menu.PERM_SEPARATOR;
import static com.github.thundax.modules.sys.entity.Menu.PERM_SUPER;
import static com.github.thundax.modules.sys.entity.Menu.PERM_USER;

import com.github.thundax.common.security.permission.PermissionMatcher;
import com.github.thundax.common.security.permission.PrefixPermissionMatcher;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.PermissionDao;
import com.github.thundax.modules.auth.entity.PermissionSession;
import com.github.thundax.modules.auth.service.PermissionService;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/** 后台权限会话服务实现。 */
@Service
@EnableConfigurationProperties(AuthProperties.class)
public class PermissionServiceImpl implements PermissionService {

    private static final int SAFETY_SECONDS = 10;

    private final PermissionDao permissionDao;
    private final AuthProperties properties;
    private final PermissionMatcher permissionMatcher = new PrefixPermissionMatcher();

    public PermissionServiceImpl(PermissionDao permissionDao, AuthProperties properties) {
        this.permissionDao = permissionDao;
        this.properties = properties;
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
        User user = UserServiceHolder.get(userId);
        Assert.notNull(user, "user can not be null");

        Set<String> permissions = new HashSet<>();
        List<Menu> menuList = UserServiceHolder.findMenuList(user);
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

    private int expiredSeconds() {
        return properties.getLoginExpiredSeconds() + SAFETY_SECONDS;
    }
}
