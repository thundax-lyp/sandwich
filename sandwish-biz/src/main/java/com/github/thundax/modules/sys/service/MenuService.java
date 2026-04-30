package com.github.thundax.modules.sys.service;

import com.github.thundax.common.service.TreeService;
import com.github.thundax.modules.sys.entity.Menu;
import java.util.List;

public interface MenuService extends TreeService<Menu> {

    /**
     * 根据等级获取
     *
     * @param maxRank 等级
     * @return 列表
     */
    List<Menu> findList(Integer maxRank);

    /**
     * 获取子菜单
     *
     * @param parentId 父节点id
     * @return 列表
     */
    List<Menu> findChildList(String parentId);

    /**
     * 更新显示状态
     *
     * @param menu 对象
     * @return 影响记录数
     */
    int updateVisibility(Menu menu);

    /**
     * 更新显示状态
     *
     * @param list 列表
     * @return 影响记录数
     */
    int updateVisibility(List<Menu> list);
}
