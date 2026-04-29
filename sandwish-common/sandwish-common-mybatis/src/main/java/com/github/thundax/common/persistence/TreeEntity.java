package com.github.thundax.common.persistence;

/**
 * @param <T>
 * @author thundax
 */
public abstract class TreeEntity<T extends TreeEntity<T>> extends DataEntity<T> {

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
}
