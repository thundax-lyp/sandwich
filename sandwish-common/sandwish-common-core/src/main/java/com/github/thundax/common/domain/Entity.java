package com.github.thundax.common.domain;

import com.github.thundax.common.id.EntityId;

public abstract class Entity<T extends Entity<T>> {

    protected EntityId id;

    public EntityId getEntityId() {
        return id;
    }

    public void setEntityId(EntityId id) {
        this.id = id;
    }
}
