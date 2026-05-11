package com.github.thundax.common.security.permission;

import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.common.security.context.SandwishSubject;
import java.util.Arrays;

public class PermissionAuthorizationService {

    private final PermissionMatcher permissionMatcher;

    public PermissionAuthorizationService(PermissionMatcher permissionMatcher) {
        this.permissionMatcher = permissionMatcher;
    }

    public boolean isPermitted(String permission) {
        return isPermittedAny(permission);
    }

    public boolean isPermittedAny(String... permissions) {
        SandwishSubject subject = SandwishContextHolder.currentSubject();
        if (subject == null || !subject.isAuthenticated() || permissions == null || permissions.length == 0) {
            return false;
        }
        return Arrays.stream(permissions)
                .filter(permission -> permission != null && !permission.trim().isEmpty())
                .anyMatch(permission -> permissionMatcher.matches(subject.getAuthorities(), permission));
    }
}
