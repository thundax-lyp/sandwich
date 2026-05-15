package com.github.thundax.modules.open.entity.valueobject;

import java.util.List;
import java.util.stream.Collectors;

public final class OpenClientPermissionIdCodec {

    private OpenClientPermissionIdCodec() {}

    public static OpenClientPermissionId toDomain(Long value) {
        return OpenClientPermissionId.ofNullable(value);
    }

    public static OpenClientPermissionId toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : OpenClientPermissionId.of(Long.valueOf(value));
    }

    public static Long toValue(OpenClientPermissionId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(OpenClientPermissionId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<OpenClientPermissionId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(OpenClientPermissionIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<OpenClientPermissionId> ids) {
        return ids == null
                ? null
                : ids.stream().map(OpenClientPermissionIdCodec::toValue).collect(Collectors.toList());
    }
}
