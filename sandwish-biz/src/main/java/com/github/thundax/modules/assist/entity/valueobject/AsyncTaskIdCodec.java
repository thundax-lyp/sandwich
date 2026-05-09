package com.github.thundax.modules.assist.entity.valueobject;

import java.util.List;
import java.util.stream.Collectors;

public final class AsyncTaskIdCodec {

    private AsyncTaskIdCodec() {}

    public static AsyncTaskId toDomain(Long value) {
        return AsyncTaskId.ofNullable(value);
    }

    public static Long toValue(AsyncTaskId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(AsyncTaskId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<AsyncTaskId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(AsyncTaskIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<AsyncTaskId> ids) {
        return ids == null ? null : ids.stream().map(AsyncTaskIdCodec::toValue).collect(Collectors.toList());
    }
}
