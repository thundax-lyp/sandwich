package com.github.thundax.modules.sys.codec;

import com.github.thundax.modules.sys.entity.valueobject.UserRank;
import org.junit.Assert;
import org.junit.Test;

public class UserRankCodecTest {

    @Test
    public void shouldConvertBetweenRankAndIntegerValue() {
        Assert.assertEquals(UserRank.of(3), UserRankCodec.toDomain(3));
        Assert.assertEquals(Integer.valueOf(3), UserRankCodec.toValue(UserRank.of(3)));
        Assert.assertEquals(Integer.valueOf(0), UserRankCodec.toValue(null));
    }
}
