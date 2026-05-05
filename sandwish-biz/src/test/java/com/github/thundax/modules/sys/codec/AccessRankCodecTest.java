package com.github.thundax.modules.sys.codec;

import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import org.junit.Assert;
import org.junit.Test;

public class AccessRankCodecTest {

    @Test
    public void shouldConvertBetweenRankAndIntegerValue() {
        Assert.assertEquals(AccessRank.of(3), AccessRankCodec.toDomain(3));
        Assert.assertEquals(Integer.valueOf(3), AccessRankCodec.toValue(AccessRank.of(3)));
        Assert.assertEquals(Integer.valueOf(0), AccessRankCodec.toValue(null));
    }
}
