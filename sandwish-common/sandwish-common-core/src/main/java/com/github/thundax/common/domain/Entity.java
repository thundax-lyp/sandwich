package com.github.thundax.common.domain;

import com.github.thundax.common.id.EntityId;
import java.util.Objects;

public abstract class Entity<T extends Entity<T>> {

    protected EntityId id;

    public EntityId getId() {
        return id;
    }

    public void setId(EntityId id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (!getClass().equals(obj.getClass())) {
            return false;
        } else if (this.getId() == null) {
            return false;
        }
        return this.getId().equals(((Entity<?>) obj).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), getId());
    }
}
