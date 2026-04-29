package com.github.thundax.common.id;

public final class EntityId extends BaseStringId {

    private EntityId(String value) {
        super(value);
    }

    public static EntityId of(String value) {
        return new EntityId(value);
    }

    public static EntityId ofNullable(String value) {
        return value == null || value.trim().isEmpty() ? null : of(value);
    }
}
