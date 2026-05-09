package com.github.thundax.modules.sys.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class DictId extends BaseLongId {

    private DictId(Long value) {
        super(value);
    }

    public static DictId of(Long value) {
        return new DictId(value);
    }

    public static DictId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
