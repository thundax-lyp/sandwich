package com.github.thundax.modules.sys.codec;

import com.github.thundax.modules.sys.entity.valueobject.UserRank;

public final class UserRankCodec {

    private UserRankCodec() {}

    public static UserRank toDomain(Integer value) {
        return UserRank.of(value);
    }

    public static Integer toValue(UserRank rank) {
        return rank == null ? UserRank.MIN_VALUE : rank.value();
    }
}
