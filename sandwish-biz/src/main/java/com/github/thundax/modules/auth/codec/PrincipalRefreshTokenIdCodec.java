package com.github.thundax.modules.auth.codec;

import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalRefreshTokenId;

public final class PrincipalRefreshTokenIdCodec {

    private PrincipalRefreshTokenIdCodec() {}

    public static PrincipalRefreshTokenId toDomain(String value) {
        return PrincipalRefreshTokenId.ofNullable(value);
    }

    public static String toValue(PrincipalRefreshTokenId id) {
        return id == null ? null : id.value();
    }

    public static PrincipalRefreshTokenId nextId(SnowflakeIdGenerator generator) {
        return toDomain(Long.toHexString(generator.nextId().value()));
    }
}
