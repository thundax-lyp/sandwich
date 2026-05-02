package com.github.thundax.common.service;

/**
 * 树形实体服务接口。
 *
 * @param <T> 实体类型
 */
public interface TreeService<T> extends CrudService<T> {

    enum MoveTreeNodeType {
        BEFORE,

        AFTER,

        INSIDE,

        INSIDE_LAST
    }

    void moveTreeNode(T fromBean, T toBean, MoveTreeNodeType moveType);

    /**
     * 判断 child 是否为 parent 的子孙节点。
     */
    boolean isChildOf(T child, T parent);
}
