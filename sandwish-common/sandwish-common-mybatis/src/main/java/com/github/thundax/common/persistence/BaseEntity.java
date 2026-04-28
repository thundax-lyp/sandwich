package com.github.thundax.common.persistence;

import java.io.Serializable;

/**
 * Entity支持类
 *
 * @author thundax
 */
public abstract class BaseEntity<T> implements Serializable {

    private static final long serialVersionUID = 1L;


    protected String id;

    public BaseEntity() {
        initialize();
    }

    public BaseEntity(String id) {
        this();
        this.id = id;
    }


    protected abstract void initialize();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public abstract void preInsert();


    public abstract void preUpdate();

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


    public static final String DEL_FLAG_NORMAL = "0";

    public static final String DEL_FLAG_DELETE = "1";
}
