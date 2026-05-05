package com.github.thundax.common.security.permission;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.security.user.CurrentUser;
import java.util.Arrays;
import org.junit.Test;

public class PermissionAuthorizationServiceTest {

    @Test
    public void shouldPermitWhenAnyRequiredPermissionMatches() {
        PermissionAuthorizationService service = new PermissionAuthorizationService(
                () -> new CurrentUser("user-1", "admin", "Admin", Arrays.asList("sys:user:view")),
                new PrefixPermissionMatcher());

        assertTrue(service.isPermittedAny("sys:role:view", "sys:user:view"));
    }

    @Test
    public void shouldDenyAnonymousUser() {
        PermissionAuthorizationService service =
                new PermissionAuthorizationService(CurrentUser::anonymous, new PrefixPermissionMatcher());

        assertFalse(service.isPermitted("sys:user:view"));
    }
}
