package com.github.thundax.modules.submission.entity.valueobject;

import java.util.List;
import java.util.stream.Collectors;

public final class SubmissionIdCodec {

    private SubmissionIdCodec() {}

    public static SubmissionId toDomain(Long value) {
        return SubmissionId.ofNullable(value);
    }

    public static SubmissionId toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : SubmissionId.of(Long.valueOf(value));
    }

    public static Long toValue(SubmissionId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(SubmissionId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<SubmissionId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(SubmissionIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<SubmissionId> ids) {
        return ids == null ? null : ids.stream().map(SubmissionIdCodec::toValue).collect(Collectors.toList());
    }
}
