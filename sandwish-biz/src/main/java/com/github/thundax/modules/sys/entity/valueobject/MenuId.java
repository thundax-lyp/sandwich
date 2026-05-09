package com.github.thundax.modules.sys.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class MenuId extends BaseLongId {

    private MenuId(Long value) {
        super(value);
    }

    public static MenuId of(Long value) {
        return new MenuId(value);
    }

    public static MenuId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
