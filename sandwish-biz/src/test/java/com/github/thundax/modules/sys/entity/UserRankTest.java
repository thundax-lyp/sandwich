package com.github.thundax.modules.sys.entity;

import org.junit.Assert;
import org.junit.Test;

public class UserRankTest {

    @Test
    public void shouldNormalizeUserRankBounds() {
        Assert.assertEquals(0, UserRank.of(null).value());
        Assert.assertEquals(0, UserRank.of(-1).value());
        Assert.assertEquals(5, UserRank.of(5).value());
        Assert.assertEquals(9, UserRank.of(9).value());
        Assert.assertEquals(9, UserRank.of(10).value());
    }

    @Test
    public void shouldCompareAccessibleRank() {
        UserRank rank = UserRank.of(3);

        Assert.assertTrue(rank.canAccess(2));
        Assert.assertTrue(rank.canAccess(3));
        Assert.assertFalse(rank.canAccess(4));
    }

    @Test
    public void shouldCompareByValue() {
        Assert.assertEquals(UserRank.of(3), UserRank.of(3));
        Assert.assertEquals(UserRank.of(3).hashCode(), UserRank.of(3).hashCode());
        Assert.assertNotEquals(UserRank.of(3), UserRank.of(4));
    }
}
