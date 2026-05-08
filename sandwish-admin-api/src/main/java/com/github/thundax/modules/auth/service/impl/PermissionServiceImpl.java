package com.github.thundax.modules.auth.service.impl;

import static com.github.thundax.modules.sys.entity.valueobject.PermissionCode.ADMIN;
import static com.github.thundax.modules.sys.entity.valueobject.PermissionCode.SEPARATOR;
import static com.github.thundax.modules.sys.entity.valueobject.PermissionCode.SUPER;
import static com.github.thundax.modules.sys.entity.valueobject.PermissionCode.USER;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.security.permission.PermissionMatcher;
import com.github.thundax.common.security.permission.PrefixPermissionMatcher;
import com.github.thundax.modules.auth.dao.PrincipalAccessTokenDao;
import com.github.thundax.modules.auth.dao.PrincipalAuthSessionDao;
import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.entity.PrincipalAuthSession;
import com.github.thundax.modules.auth.service.PermissionService;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.valueobject.PermissionCode;
import com.github.thundax.modules.sys.service.CurrentUserService;
import com.github.thundax.modules.sys.service.UserService;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class PermissionServiceImpl implements PermissionService {

    private static final int SAFETY_SECONDS = 10;

    private final PrincipalAccessTokenDao principalAccessTokenDao;
    private final PrincipalAuthSessionDao principalAuthSessionDao;
    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final PermissionMatcher permissionMatcher = new PrefixPermissionMatcher();

    public PermissionServiceImpl(
            PrincipalAccessTokenDao principalAccessTokenDao,
            PrincipalAuthSessionDao principalAuthSessionDao,
            UserService userService,
            CurrentUserService currentUserService) {
        this.principalAccessTokenDao = principalAccessTokenDao;
        this.principalAuthSessionDao = principalAuthSessionDao;
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @Override
    public Set<String> createPermissions(String token, String userId) {
        Assert.hasText(token, "token can not be empty");
        Assert.hasText(userId, "userId can not be empty");

        PrincipalAuthSession session = getActiveSession(token);
        if (session == null) {
            return Collections.emptySet();
        }
        Set<String> permissions = loadPermissions(userId);
        session.getValues()
                .put(
                        PrincipalAuthSession.VALUE_PERMISSIONS,
                        new PrincipalAuthSession.PrincipalAuthSessionValue(permissions, session.getExpireAt()));
        principalAuthSessionDao.insert(session, expiredSeconds(session));
        return permissions;
    }

    @Override
    public Set<String> getPermissions(String token) {
        PrincipalAuthSession session = getActiveSession(token);
        if (session == null) {
            return null;
        }
        PrincipalAuthSession.PrincipalAuthSessionValue value =
                session.getValues().get(PrincipalAuthSession.VALUE_PERMISSIONS);
        if (value == null || isExpired(value.getExpiredAt())) {
            return null;
        }
        return toPermissionSet(value.getValue());
    }

    @Override
    public boolean isPermitted(String token, String permission) {
        return permissionMatcher.matches(getPermissions(token), permission);
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

    private PrincipalAuthSession getActiveSession(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        PrincipalAccessToken accessToken = principalAccessTokenDao.getByToken(token);
        if (accessToken == null || accessToken.getSessionId() == null || !accessToken.canAccess(new Date())) {
            return null;
        }
        PrincipalAuthSession session = principalAuthSessionDao.getById(accessToken.getSessionId());
        if (session == null || session.isExpired(new Date())) {
            return null;
        }
        return session;
    }

    private boolean isExpired(Date expiredAt) {
        return expiredAt != null && !expiredAt.after(new Date());
    }

    private Set<String> toPermissionSet(Object value) {
        if (!(value instanceof Collection)) {
            return null;
        }
        Set<String> permissions = new HashSet<>();
        for (Object item : (Collection<?>) value) {
            if (item != null) {
                permissions.add(String.valueOf(item));
            }
        }
        return permissions;
    }

    private int expiredSeconds(PrincipalAuthSession session) {
        return session.remainingSeconds(new Date()) + SAFETY_SECONDS;
    }
}
