package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.modules.sys.entity.Menu;

/**
 * @author wdit
 */
public interface MenuDao extends CrudDao<Menu> {

    void moveTreeNode(String fromId, String toId, TreeService.MoveTreeNodeType moveType);

    boolean isChildOf(String childId, String parentId);

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
