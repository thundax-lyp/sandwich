package com.github.thundax.common.persistence;

import com.github.thundax.common.domain.Entity;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import java.io.Serializable;

/**
 * Entity支持类
 *
 * @author thundax
 */
public abstract class BaseEntity<T extends BaseEntity<T>> extends Entity<T> implements Serializable {

    public BaseEntity() {
        initialize();
    }

    public BaseEntity(String id) {
        this();
        setEntityId(EntityIdCodec.toDomain(id));
    }

    public BaseEntity(EntityId id) {
        this();
        setEntityId(id);
    }

    protected abstract void initialize();

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (!getClass().equals(obj.getClass())) {
            return false;
        } else if (this.getEntityId() == null) {
            return false;
        }
        return this.getEntityId().equals(((BaseEntity<?>) obj).getEntityId());
    }
}
