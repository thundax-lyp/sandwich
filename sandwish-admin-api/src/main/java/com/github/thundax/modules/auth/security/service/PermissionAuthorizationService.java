package com.github.thundax.modules.auth.security.service;

import com.github.thundax.common.security.permission.PermissionAuthorities;
import com.github.thundax.common.security.permission.PermissionMatcher;
import com.github.thundax.common.security.permission.PrefixPermissionMatcher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Spring Security 权限表达式服务。 */
@Service
public class PermissionAuthorizationService {

    private final PermissionMatcher permissionMatcher = new PrefixPermissionMatcher();

    public boolean isPermitted(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && permissionMatcher.matches(
                        PermissionAuthorities.toPermissions(authentication.getAuthorities()), permission);
    }
}
