package com.github.thundax.common.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thundax.common.utils.StringUtils;
import java.io.Serializable;

/**
 * Entity支持类
 *
 * @author thundax
 */
public abstract class BaseEntity<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 实体编号（唯一标识） */
    protected String id;

    /** 是否是新记录（默认：false），调用setIsNewRecord()设置新记录，使用自定义ID。 设置为true后强制执行插入语句，ID不会自动生成，需从手动传入。 */
    protected boolean isNewRecord = false;

    public BaseEntity() {
        initialize();
    }

    public BaseEntity(String id) {
        this();
        this.id = id;
    }

    /** 初始化方法 */
    protected abstract void initialize();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** 插入之前执行方法，子类实现 */
    public abstract void preInsert();

    /** 更新之前执行方法，子类实现 */
    public abstract void preUpdate();

    /** 是否是新记录（默认：false），调用setIsNewRecord()设置新记录，使用自定义ID。 设置为true后强制执行插入语句，ID不会自动生成，需从手动传入。 */
    @JsonIgnore
    public boolean getIsNewRecord() {
        return isNewRecord || StringUtils.isBlank(getId());
    }

    /** 是否是新记录（默认：false），调用setIsNewRecord()设置新记录，使用自定义ID。 设置为true后强制执行插入语句，ID不会自动生成，需从手动传入。 */
    public void setIsNewRecord(boolean isNewRecord) {
        this.isNewRecord = isNewRecord;
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

    /** 删除标记（0：正常；1：删除；） */
    public static final String DEL_FLAG_NORMAL = "0";

    public static final String DEL_FLAG_DELETE = "1";
}
