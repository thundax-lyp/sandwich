package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.modules.sys.entity.Menu;
import java.util.List;

public interface MenuDao {

    Menu get(String id);

    List<Menu> getMany(List<String> idList);

    List<Menu> findList(String parentId, String displayFlag, Integer maxRank);

    Page<Menu> findPage(String parentId, String displayFlag, Integer maxRank, int pageNo, int pageSize);

    String insert(Menu menu);

    int update(Menu menu);

    int updatePriority(Menu menu);

    int updateDelFlag(Menu menu);

    int delete(String id);

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
    void deleteMenuRole(String menuId);
}
