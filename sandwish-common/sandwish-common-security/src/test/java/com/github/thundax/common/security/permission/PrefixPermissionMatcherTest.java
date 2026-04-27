package com.github.thundax.common.security.permission;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class PrefixPermissionMatcherTest {

    private final PrefixPermissionMatcher matcher = new PrefixPermissionMatcher();

    @Test
    public void shouldMatchSamePermission() {
        Assert.assertTrue(matcher.matches("sys:role:view", "sys:role:view"));
    }

    @Test
    public void shouldMatchChildPermissionWhenParentPermissionExists() {
        Assert.assertTrue(matcher.matches("sys:role", "sys:role:view"));
        Assert.assertTrue(matcher.matches("sys", "sys:role:view"));
    }

    @Test
    public void shouldNotMatchParentPermissionWhenOnlyChildPermissionExists() {
        Assert.assertFalse(matcher.matches("sys:role:view", "sys:role"));
    }

    @Test
    public void shouldNotMatchPermissionWithDifferentSegmentPrefix() {
        Assert.assertFalse(matcher.matches("sys:role", "sys:roleManage:view"));
    }

    @Test
    public void shouldMatchAnyOwnedPermission() {
        Assert.assertTrue(matcher.matches(Arrays.asList("user", "sys:role"), "sys:role:edit"));
    }

    @Test
    public void shouldRejectBlankOrEmptyPermission() {
        Assert.assertFalse(matcher.matches(Collections.<String>emptyList(), "sys:role:view"));
        Assert.assertFalse(matcher.matches(Arrays.asList(" ", null), "sys:role:view"));
        Assert.assertFalse(matcher.matches("sys:role", " "));
    }
}
