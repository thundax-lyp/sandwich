package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.TreeDao;
import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.MenuDO;

/**
 * 菜单 MyBatis Mapper。
 */
@MyBatisDao
public interface MenuMapper extends TreeDao<MenuDO> {

    /**
     * 更新 displayFlag。
     *
     * @param menu 菜单
     * @return 影响记录数
     */
    int updateDisplayFlag(MenuDO menu);

    /**
     * 删除菜单权限关系。
     *
     * @param menu 菜单
     */
    void deleteMenuRole(MenuDO menu);
}
