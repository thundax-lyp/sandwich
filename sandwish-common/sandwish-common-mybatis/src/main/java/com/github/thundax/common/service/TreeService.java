package com.github.thundax.common.service;

/**
 * @param <T> entity
 * @author thundax
 */
public interface TreeService<T> extends CrudService<T> {

    enum MoveTreeNodeType {
        BEFORE,

        AFTER,

        INSIDE,

        INSIDE_LAST
    }

    /**
     * 移动节点
     *
     * @param fromBean 源节点
     * @param toBean 目标节点
     * @param moveType 移动类型
     */
    void moveTreeNode(T fromBean, T toBean, MoveTreeNodeType moveType);

    /**
     * 判断 child 是否为 parent 的子孙节点。
     *
     * @param child 子节点
     * @param parent 父节点
     * @return 是否为子孙节点
     */
    boolean isChildOf(T child, T parent);
}
