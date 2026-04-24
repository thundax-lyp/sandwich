package com.github.thundax.common.service;

/**
 * @param <T> entity
 * @author thundax
 */
public interface TreeService<T> extends CrudService<T> {

    enum MoveTreeNodeType {
        /**
         * 移动到节点前
         */
        BEFORE,
        /**
         * 移动到节点后
         */
        AFTER,
        /**
         * 移动为子节点
         */
        INSIDE,
        /**
         * 移动为子节点，并到最后
         */
        INSIDE_LAST
    }

    /**
     * 移动节点
     *
     * @param fromBean 源节点
     * @param toBean   目标节点
     * @param moveType 移动类型
     */
    void moveTreeNode(T fromBean, T toBean, MoveTreeNodeType moveType);

    /**
     * 更新左右树结构，只在初始化时使用
     *
     * @param entity entity
     */
    void updateLftRgt(T entity);

}
