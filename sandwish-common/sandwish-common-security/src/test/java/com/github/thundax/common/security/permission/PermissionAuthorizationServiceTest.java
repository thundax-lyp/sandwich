package com.github.thundax.common.security.permission;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.common.security.context.SandwishSubject;
import com.github.thundax.common.security.context.SandwishSubjectType;
import java.util.Arrays;
import org.junit.After;
import org.junit.Test;

public class PermissionAuthorizationServiceTest {

    @After
    public void tearDown() {
        SandwishContextHolder.clear();
    }

    @Test
    public void shouldPermitWhenAnyRequiredPermissionMatches() {
        SandwishContextHolder.setSubject(new SandwishSubject(
                "user-1", SandwishSubjectType.ADMIN_USER, "Admin", "token-1", Arrays.asList("sys:user:view")));
        PermissionAuthorizationService service = new PermissionAuthorizationService(new PrefixPermissionMatcher());

        assertTrue(service.isPermittedAny("sys:role:view", "sys:user:view"));
    }

    @Test
    public void shouldDenyAnonymousUser() {
        PermissionAuthorizationService service = new PermissionAuthorizationService(new PrefixPermissionMatcher());

        assertFalse(service.isPermitted("sys:user:view"));
    }
}
