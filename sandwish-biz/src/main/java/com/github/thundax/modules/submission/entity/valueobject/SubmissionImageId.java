package com.github.thundax.modules.submission.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class SubmissionImageId extends BaseLongId {

    private SubmissionImageId(Long value) {
        super(value);
    }

    public static SubmissionImageId of(Long value) {
        return new SubmissionImageId(value);
    }

    public static SubmissionImageId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
