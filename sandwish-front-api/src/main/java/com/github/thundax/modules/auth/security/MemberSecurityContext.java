package com.github.thundax.modules.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class MemberSecurityContext {

    private MemberSecurityContext() {}

    public static MemberSpringPrincipal getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof MemberSpringPrincipal) {
            return (MemberSpringPrincipal) authentication.getPrincipal();
        }

        return null;
    }

    public static String getCurrentMemberId() {
        MemberSpringPrincipal principal = getPrincipal();
        if (principal == null) {
            return null;
        }
        return principal.getId();
    }
}
