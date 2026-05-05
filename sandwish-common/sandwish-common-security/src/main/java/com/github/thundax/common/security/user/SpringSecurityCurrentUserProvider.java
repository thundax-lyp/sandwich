package com.github.thundax.common.security.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SpringSecurityCurrentUserProvider implements CurrentUserProvider {

    private final CurrentUserResolver resolver;

    public SpringSecurityCurrentUserProvider() {
        this(new SecurityContextCurrentUserResolver());
    }

    public SpringSecurityCurrentUserProvider(CurrentUserResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public CurrentUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return resolver.resolve(authentication);
    }
}
