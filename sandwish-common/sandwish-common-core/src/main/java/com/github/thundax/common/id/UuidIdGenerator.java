package com.github.thundax.common.id;

public class UuidIdGenerator implements IdGenerator {

    @Override
    public EntityId nextId() {
        return EntityId.of(UuidHelper.compact());
    }
}
