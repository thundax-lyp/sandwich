package com.github.thundax.common.security.permission;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Set;

public class PermissionAuthoritiesTest {

    @Test
    public void shouldConvertSpringAuthoritiesToPermissions() {
        Set<String> permissions = PermissionAuthorities.toPermissions(Arrays.asList(
                new SimpleGrantedAuthority("user"),
                new SimpleGrantedAuthority("sys:role")));

        Assert.assertTrue(permissions.contains("user"));
        Assert.assertTrue(permissions.contains("sys:role"));
        Assert.assertEquals(2, permissions.size());
    }
}
