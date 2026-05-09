package com.github.thundax.modules.sys.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class RoleId extends BaseLongId {

    private RoleId(Long value) {
        super(value);
    }

    public static RoleId of(Long value) {
        return new RoleId(value);
    }

    public static RoleId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
