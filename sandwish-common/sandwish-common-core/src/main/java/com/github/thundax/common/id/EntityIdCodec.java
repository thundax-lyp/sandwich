package com.github.thundax.common.id;

public final class EntityIdCodec {

    private EntityIdCodec() {}

    public static EntityId toDomain(String value) {
        return EntityId.ofNullable(value);
    }

    public static String toValue(EntityId entityId) {
        return entityId == null ? null : entityId.value();
    }
}
