package com.github.thundax.modules.open.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class OpenClientId extends BaseLongId {

    private OpenClientId(Long value) {
        super(value);
    }

    public static OpenClientId of(Long value) {
        return new OpenClientId(value);
    }

    public static OpenClientId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
