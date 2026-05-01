package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.modules.sys.entity.Menu;
import java.util.List;

public interface MenuDao {

    Menu getById(EntityId id);

    List<Menu> batchGetByIds(List<String> idList);

    List<Menu> list(String parentId, String displayFlag, Integer maxRank);

    Page<Menu> page(String parentId, String displayFlag, Integer maxRank, int pageNo, int pageSize);

    String insert(Menu menu);

    int update(Menu menu);

    int updatePriority(Menu menu);

    int deleteById(EntityId id);

    void moveTreeNode(String fromId, String toId, TreeService.MoveTreeNodeType moveType);

    boolean isChildOf(String childId, String parentId);

    /**
     * 更新显示状态
     *
     * @param menu 菜单
     * @return 影响记录数
     */
    int updateVisibility(Menu menu);

    /**
     * 删除菜单权限关系
     *
     * @param menu 菜单
     */
    void deleteMenuRole(String menuId);
}
