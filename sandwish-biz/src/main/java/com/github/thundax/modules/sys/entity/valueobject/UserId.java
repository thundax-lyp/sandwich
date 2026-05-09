package com.github.thundax.modules.sys.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class UserId extends BaseLongId {

    private UserId(Long value) {
        super(value);
    }

    public static UserId of(Long value) {
        return new UserId(value);
    }

    public static UserId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
