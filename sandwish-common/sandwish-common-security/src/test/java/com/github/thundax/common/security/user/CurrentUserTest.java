package com.github.thundax.common.security.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

public class CurrentUserTest {

    @Test
    public void shouldExposeStableIdentityAndAuthorities() {
        CurrentUser currentUser = new CurrentUser("user-1", "admin", "Admin", Arrays.asList("sys:user", "sys:role"));

        assertTrue(currentUser.isAuthenticated());
        assertTrue(currentUser.hasAuthority("sys:user"));
        assertTrue(currentUser.hasAuthority("sys:role"));
        assertFalse(currentUser.hasAuthority("sys:menu"));
    }

    @Test
    public void shouldIgnoreBlankAuthorities() {
        CurrentUser currentUser = new CurrentUser("user-1", "admin", "Admin", Arrays.asList("sys:user", "", null));

        assertTrue(currentUser.hasAuthority("sys:user"));
        assertFalse(currentUser.hasAuthority(""));
    }

    @Test
    public void shouldProvideAnonymousUser() {
        CurrentUser currentUser = new AnonymousCurrentUserProvider().currentUser();

        assertFalse(currentUser.isAuthenticated());
        assertTrue(currentUser.getAuthorities().isEmpty());
    }
}
