package com.github.thundax.modules.submission.entity.valueobject;

import java.util.List;
import java.util.stream.Collectors;

public final class SubmissionImageIdCodec {

    private SubmissionImageIdCodec() {}

    public static SubmissionImageId toDomain(Long value) {
        return SubmissionImageId.ofNullable(value);
    }

    public static Long toValue(SubmissionImageId id) {
        return id == null ? null : id.value();
    }

    public static List<Long> toValues(List<SubmissionImageId> ids) {
        return ids == null
                ? null
                : ids.stream().map(SubmissionImageIdCodec::toValue).collect(Collectors.toList());
    }
}
