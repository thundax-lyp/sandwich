package com.github.thundax.common.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.function.BiPredicate;

/**
 * @param <T>
 * @author thundax
 */
public abstract class TreeEntity<T> extends DataEntity<T> {

    public static final String ROOT_ID = "ROOT";

    public TreeEntity() {
        super();
    }

    public TreeEntity(String id) {
        super(id);
    }

    /**
     * 获取父节点id
     *
     * @return parentId
     */
    public abstract String getParentId();

    /**
     * 设置父节点id
     *
     * @param parentId parentId
     */
    public abstract void setParentId(String parentId);

    /**
     * 获取lft
     *
     * @return lft
     */
    public abstract Integer getLft();

    /**
     * 设置lft
     *
     * @param lft lft
     */
    public abstract void setLft(Integer lft);

    /**
     * 获取rgt
     *
     * @return rgt
     */
    public abstract Integer getRgt();

    /**
     * 设置rgt
     *
     * @param rgt rgt
     */
    public abstract void setRgt(Integer rgt);

    @JsonIgnore
    public Integer treeSpan() {
        return this.getRgt() - this.getLft() + 1;
    }

    @JsonIgnore
    public boolean hasChild() {
        return this.getRgt() - this.getLft() > 1;
    }

    @JsonIgnore
    public boolean isChildOf(TreeEntity<T> that, BiPredicate<TreeEntity<T>, TreeEntity<T>> validator) {
        if (validator != null && !validator.test(this, that)) {
            return false;
        }
        return this.getLft() > that.getLft() && this.getRgt() < that.getRgt();
    }

}
