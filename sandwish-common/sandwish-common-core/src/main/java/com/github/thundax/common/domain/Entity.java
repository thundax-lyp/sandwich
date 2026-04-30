package com.github.thundax.common.domain;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import java.util.Objects;

public abstract class Entity<T extends Entity<T>> {

    protected EntityId id;

    public EntityId getEntityId() {
        return id;
    }

    public void setEntityId(EntityId id) {
        this.id = id;
    }

    public String getId() {
        return EntityIdCodec.toValue(getEntityId());
    }

    public void setId(String id) {
        setEntityId(EntityIdCodec.toDomain(id));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (!getClass().equals(obj.getClass())) {
            return false;
        } else if (this.getEntityId() == null) {
            return false;
        }
        return this.getEntityId().equals(((Entity<?>) obj).getEntityId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), getEntityId());
    }
}
