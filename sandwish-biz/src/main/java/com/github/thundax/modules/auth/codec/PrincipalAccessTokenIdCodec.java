package com.github.thundax.modules.auth.codec;

import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalAccessTokenId;

public final class PrincipalAccessTokenIdCodec {

    private PrincipalAccessTokenIdCodec() {}

    public static PrincipalAccessTokenId toDomain(String value) {
        return PrincipalAccessTokenId.ofNullable(value);
    }

    public static String toValue(PrincipalAccessTokenId id) {
        return id == null ? null : id.value();
    }

    public static PrincipalAccessTokenId nextId(SnowflakeIdGenerator generator) {
        return toDomain(Long.toHexString(generator.nextId().value()));
    }
}
