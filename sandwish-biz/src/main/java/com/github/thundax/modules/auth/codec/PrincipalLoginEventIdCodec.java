package com.github.thundax.modules.auth.codec;

import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalLoginEventId;

public final class PrincipalLoginEventIdCodec {

    private PrincipalLoginEventIdCodec() {}

    public static PrincipalLoginEventId toDomain(String value) {
        return PrincipalLoginEventId.ofNullable(value);
    }

    public static String toValue(PrincipalLoginEventId id) {
        return id == null ? null : id.value();
    }

    public static PrincipalLoginEventId nextId(SnowflakeIdGenerator generator) {
        return toDomain(Long.toHexString(generator.nextId().value()));
    }
}
