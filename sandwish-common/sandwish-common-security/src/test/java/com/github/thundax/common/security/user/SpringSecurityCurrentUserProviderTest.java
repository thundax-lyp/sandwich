package com.github.thundax.common.security.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class SpringSecurityCurrentUserProviderTest {

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldResolveCurrentUserFromSecurityContext() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("admin", "credential", "sys:user:view"));

        CurrentUser currentUser = new SpringSecurityCurrentUserProvider().currentUser();

        assertEquals("admin", currentUser.getUserId());
    }

    @Test
    public void shouldReturnAnonymousWhenSecurityContextIsEmpty() {
        CurrentUser currentUser = new SpringSecurityCurrentUserProvider().currentUser();

        assertFalse(currentUser.isAuthenticated());
    }
}
