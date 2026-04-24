package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.persistence.TreeDao;
import com.github.thundax.modules.sys.entity.Menu;

/**
 * @author wdit
 */
public interface MenuDao extends TreeDao<Menu> {

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
