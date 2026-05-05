package com.github.thundax.modules.sys.codec;

import com.github.thundax.modules.sys.entity.valueobject.AccessRank;

public final class AccessRankCodec {

    private AccessRankCodec() {}

    public static AccessRank toDomain(Integer value) {
        return AccessRank.of(value);
    }

    public static Integer toValue(AccessRank rank) {
        return rank == null ? AccessRank.MIN_VALUE : rank.value();
    }
}
