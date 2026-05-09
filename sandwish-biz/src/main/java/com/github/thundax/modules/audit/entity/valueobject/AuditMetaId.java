package com.github.thundax.modules.audit.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class AuditMetaId extends BaseLongId {

    private AuditMetaId(Long value) {
        super(value);
    }

    public static AuditMetaId of(Long value) {
        return new AuditMetaId(value);
    }

    public static AuditMetaId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
