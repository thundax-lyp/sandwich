package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.modules.sys.entity.Menu;
import java.util.List;

public interface MenuDao {

    Menu getById(EntityId id);

    List<Menu> listByIds(List<String> idList);

    List<Menu> list(String parentId, String displayFlag, Integer maxRank);

    Page<Menu> page(String parentId, String displayFlag, Integer maxRank, int pageNo, int pageSize);

    String insert(Menu menu);

    int update(Menu menu);

    int updatePriority(Menu menu);

    int deleteById(EntityId id);

    void moveTreeNode(String fromId, String toId, TreeNodeMoveType moveType);

    boolean isChildOf(String childId, String parentId);

    int updateVisibility(Menu menu);

    void deleteMenuRole(String menuId);
}
