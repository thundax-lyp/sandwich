package com.github.thundax.modules.auth.codec;

import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalAuthSessionId;

public final class PrincipalAuthSessionIdCodec {

    private PrincipalAuthSessionIdCodec() {}

    public static PrincipalAuthSessionId toDomain(String value) {
        return PrincipalAuthSessionId.ofNullable(value);
    }

    public static String toValue(PrincipalAuthSessionId id) {
        return id == null ? null : id.value();
    }

    public static PrincipalAuthSessionId nextId(SnowflakeIdGenerator generator) {
        return toDomain(Long.toHexString(generator.nextId().value()));
    }
}
