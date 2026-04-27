package com.github.thundax.common.security.permission;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 转换 Spring Security 权限对象。
 */
public final class PermissionAuthorities {

    private PermissionAuthorities() {
    }

    public static Set<String> toPermissions(Collection<? extends GrantedAuthority> authorities) {
        Set<String> permissions = new LinkedHashSet<>();
        if (authorities == null || authorities.isEmpty()) {
            return permissions;
        }

        for (GrantedAuthority authority : authorities) {
            if (authority != null && authority.getAuthority() != null && !authority.getAuthority().trim().isEmpty()) {
                permissions.add(authority.getAuthority());
            }
        }

        return permissions;
    }
}
