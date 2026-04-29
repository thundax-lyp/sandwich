package com.github.thundax.common.persistence;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import java.io.Serializable;

/**
 * Entity支持类
 *
 * @author thundax
 */
public abstract class BaseEntity<T> implements Serializable {

    protected EntityId id;

    public BaseEntity() {
        initialize();
    }

    public BaseEntity(String id) {
        this();
        this.id = EntityIdCodec.toDomain(id);
    }

    public BaseEntity(EntityId id) {
        this();
        this.id = id;
    }

    protected abstract void initialize();

    public String getId() {
        return EntityIdCodec.toValue(id);
    }

    public EntityId getEntityId() {
        return id;
    }

    public void setId(String id) {
        this.id = EntityIdCodec.toDomain(id);
    }

    public void setId(EntityId id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (!getClass().equals(obj.getClass())) {
            return false;
        } else if (this.getId() == null) {
            return false;
        }
        return this.getId().equals(((BaseEntity<?>) obj).getId());
    }
}
