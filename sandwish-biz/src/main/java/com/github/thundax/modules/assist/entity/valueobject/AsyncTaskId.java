package com.github.thundax.modules.assist.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class AsyncTaskId extends BaseLongId {

    private AsyncTaskId(Long value) {
        super(value);
    }

    public static AsyncTaskId of(Long value) {
        return new AsyncTaskId(value);
    }

    public static AsyncTaskId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
