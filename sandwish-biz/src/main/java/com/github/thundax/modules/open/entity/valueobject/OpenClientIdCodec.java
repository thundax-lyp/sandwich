package com.github.thundax.modules.open.entity.valueobject;

import java.util.List;
import java.util.stream.Collectors;

public final class OpenClientIdCodec {

    private OpenClientIdCodec() {}

    public static OpenClientId toDomain(Long value) {
        return OpenClientId.ofNullable(value);
    }

    public static OpenClientId toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : OpenClientId.of(Long.valueOf(value));
    }

    public static Long toValue(OpenClientId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(OpenClientId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<OpenClientId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(OpenClientIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<OpenClientId> ids) {
        return ids == null ? null : ids.stream().map(OpenClientIdCodec::toValue).collect(Collectors.toList());
    }
}
