package com.github.thundax.modules.sys.entity.valueobject;

import org.junit.Assert;
import org.junit.Test;

public class AccessRankTest {

    @Test
    public void shouldNormalizeAccessRankBounds() {
        Assert.assertEquals(0, AccessRank.of(null).value());
        Assert.assertEquals(0, AccessRank.of(-1).value());
        Assert.assertEquals(5, AccessRank.of(5).value());
        Assert.assertEquals(9, AccessRank.of(9).value());
        Assert.assertEquals(9, AccessRank.of(10).value());
    }

    @Test
    public void shouldCompareAccessibleRank() {
        AccessRank rank = AccessRank.of(3);

        Assert.assertTrue(rank.canAccess(2));
        Assert.assertTrue(rank.canAccess(3));
        Assert.assertFalse(rank.canAccess(4));
        Assert.assertTrue(rank.canAccess(AccessRank.of(3)));
        Assert.assertFalse(rank.canAccess(AccessRank.of(4)));
    }

    @Test
    public void shouldCompareByValue() {
        Assert.assertEquals(AccessRank.of(3), AccessRank.of(3));
        Assert.assertEquals(AccessRank.of(3).hashCode(), AccessRank.of(3).hashCode());
        Assert.assertNotEquals(AccessRank.of(3), AccessRank.of(4));
    }
}
