package com.github.thundax.common.security.permission;

import com.github.thundax.common.security.user.CurrentUser;
import com.github.thundax.common.security.user.CurrentUserProvider;
import java.util.Arrays;

public class PermissionAuthorizationService {

    private final CurrentUserProvider currentUserProvider;
    private final PermissionMatcher permissionMatcher;

    public PermissionAuthorizationService(
            CurrentUserProvider currentUserProvider, PermissionMatcher permissionMatcher) {
        this.currentUserProvider = currentUserProvider;
        this.permissionMatcher = permissionMatcher;
    }

    public boolean isPermitted(String permission) {
        return isPermittedAny(permission);
    }

    public boolean isPermittedAny(String... permissions) {
        CurrentUser currentUser = currentUserProvider.currentUser();
        if (currentUser == null || !currentUser.isAuthenticated() || permissions == null || permissions.length == 0) {
            return false;
        }
        return Arrays.stream(permissions)
                .filter(permission -> permission != null && !permission.trim().isEmpty())
                .anyMatch(permission -> permissionMatcher.matches(currentUser.getAuthorities(), permission));
    }
}
