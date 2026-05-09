package com.github.thundax.modules.audit.entity.valueobject;

import java.util.List;
import java.util.stream.Collectors;

public final class AuditLogIdCodec {

    private AuditLogIdCodec() {}

    public static AuditLogId toDomain(Long value) {
        return AuditLogId.ofNullable(value);
    }

    public static Long toValue(AuditLogId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(AuditLogId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<AuditLogId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(AuditLogIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<AuditLogId> ids) {
        return ids == null ? null : ids.stream().map(AuditLogIdCodec::toValue).collect(Collectors.toList());
    }
}
