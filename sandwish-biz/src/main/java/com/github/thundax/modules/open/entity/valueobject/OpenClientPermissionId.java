package com.github.thundax.modules.open.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class OpenClientPermissionId extends BaseLongId {

    private OpenClientPermissionId(Long value) {
        super(value);
    }

    public static OpenClientPermissionId of(Long value) {
        return new OpenClientPermissionId(value);
    }

    public static OpenClientPermissionId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
