package com.github.thundax.common.security.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class SecurityContextCurrentUserResolverTest {

    private final SecurityContextCurrentUserResolver resolver = new SecurityContextCurrentUserResolver();

    @Test
    public void shouldResolveCurrentUserPrincipal() {
        CurrentUser principal = new CurrentUser("user-1", "admin", "Admin", Arrays.asList("old"));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                principal, "credential", Arrays.asList(new SimpleGrantedAuthority("sys:user:view")));

        CurrentUser currentUser = resolver.resolve(authentication);

        assertEquals("user-1", currentUser.getUserId());
        assertEquals("admin", currentUser.getLoginName());
        assertTrue(currentUser.hasAuthority("sys:user:view"));
        assertFalse(currentUser.hasAuthority("old"));
    }

    @Test
    public void shouldResolveUserDetailsPrincipal() {
        User principal = new User("admin", "password", Arrays.asList(new SimpleGrantedAuthority("sys:user:view")));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                principal, "credential", Arrays.asList(new SimpleGrantedAuthority("sys:user:view")));

        CurrentUser currentUser = resolver.resolve(authentication);

        assertEquals("admin", currentUser.getUserId());
        assertEquals("admin", currentUser.getLoginName());
        assertTrue(currentUser.hasAuthority("sys:user:view"));
    }

    @Test
    public void shouldResolveStringPrincipal() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                "admin", "credential", Arrays.asList(new SimpleGrantedAuthority("sys:user:view")));

        CurrentUser currentUser = resolver.resolve(authentication);

        assertEquals("admin", currentUser.getUserId());
        assertEquals("admin", currentUser.getLoginName());
        assertTrue(currentUser.hasAuthority("sys:user:view"));
    }

    @Test
    public void shouldResolveAnonymousForNullAuthentication() {
        assertFalse(resolver.resolve(null).isAuthenticated());
    }

    @Test
    public void shouldResolveAnonymousForAnonymousAuthentication() {
        AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken(
                "key", "anonymous", Arrays.asList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

        assertFalse(resolver.resolve(authentication).isAuthenticated());
    }
}
