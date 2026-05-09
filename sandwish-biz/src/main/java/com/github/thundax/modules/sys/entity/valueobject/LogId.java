package com.github.thundax.modules.sys.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class LogId extends BaseLongId {

    private LogId(Long value) {
        super(value);
    }

    public static LogId of(Long value) {
        return new LogId(value);
    }

    public static LogId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
