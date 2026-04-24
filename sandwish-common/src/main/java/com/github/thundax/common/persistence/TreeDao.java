package com.github.thundax.common.persistence;

import org.apache.ibatis.annotations.Param;

/**
 * @param <T>
 * @author thundax
 */
public interface TreeDao<T> extends CrudDao<T> {

    /**
     * 获取 id, parent.id, lft, rgt
     *
     * @param id id
     * @return 节点
     */
    T getTreeNode(String id);

    /**
     * 更新左右节点数
     *
     * @param node 节点
     */
    void updateLftRgt(@Param("node") T node);

    /**
     * 更新 parentId
     *
     * @param node 节点
     */
    void updateParent(@Param("node") T node);

    /**
     * 获取最大rgt
     *
     * @param node 节点
     * @return 最大rgt
     */
    Integer getMaxPosition(@Param("node") T node);

    /**
     * 批量更新 rgt
     * update table set rgt = rgt + offset where rgt >= from
     *
     * @param node   节点
     * @param from   开始位置
     * @param offset 偏移量
     */
    void moveTreeRgts(@Param("node") T node,
                      @Param("from") Integer from,
                      @Param("offset") Integer offset);

    /**
     * 批量更新 lft
     * update table set lft = lft + offset where lft >= from
     *
     * @param node   节点
     * @param from   开始位置
     * @param offset 偏移量
     */
    void moveTreeLfts(@Param("node") T node,
                      @Param("from") Integer from,
                      @Param("offset") Integer offset);

    /**
     * 批量更新lft, rgt
     * update table set lft = lft + #{offset}, rgt = rgt + #{offset} WHERE lft
     * BETWEEN #{from} ADN #{to}
     *
     * @param node   节点
     * @param from   开始位置
     * @param to     结束位置
     * @param offset 偏移量
     */
    void moveTreeNodes(@Param("node") T node,
                       @Param("from") Integer from,
                       @Param("to") Integer to,
                       @Param("offset") Integer offset);
}
