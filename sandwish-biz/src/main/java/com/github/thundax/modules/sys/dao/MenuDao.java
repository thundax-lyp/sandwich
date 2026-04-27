package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.modules.sys.entity.Menu;

/**
 * @author wdit
 */
public interface MenuDao extends CrudDao<Menu> {

    Menu getTreeNode(String id);

    void updateLftRgt(Menu node);

    void updateParent(Menu node);

    Integer getMaxPosition();

    void moveTreeRgts(Integer from, Integer offset);

    void moveTreeLfts(Integer from, Integer offset);

    void moveTreeNodes(Integer from, Integer to, Integer offset);

    /**
     * 更新 displayFlag
     *
     * @param menu 菜单
     * @return 影响记录数
     */
    int updateDisplayFlag(Menu menu);

    /**
     * 删除菜单权限关系
     *
     * @param menu 菜单
     */
    void deleteMenuRole(Menu menu);

}
