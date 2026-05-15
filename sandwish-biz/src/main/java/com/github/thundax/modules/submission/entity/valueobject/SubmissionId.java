package com.github.thundax.modules.submission.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class SubmissionId extends BaseLongId {

    private SubmissionId(Long value) {
        super(value);
    }

    public static SubmissionId of(Long value) {
        return new SubmissionId(value);
    }

    public static SubmissionId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
