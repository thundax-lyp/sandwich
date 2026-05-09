package com.github.thundax.modules.storage.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class StoredObjectId extends BaseLongId {

    private StoredObjectId(Long value) {
        super(value);
    }

    public static StoredObjectId of(Long value) {
        return new StoredObjectId(value);
    }

    public static StoredObjectId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
