package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import java.util.List;

public interface MenuService {

    Menu getById(EntityId id);

    List<Menu> listByIds(List<EntityId> ids);

    List<Menu> list(MenuQuery query);

    PageDTO<Menu> page(MenuQuery query, PageDTO<Menu> page);

    EntityId add(Menu menu);

    void update(Menu menu);

    int batchDeleteById(List<EntityId> ids);

    int updateVisibility(Menu menu);

    int updateVisibility(List<Menu> list);

    void moveTreeNode(Menu fromBean, Menu toBean, TreeNodeMoveType moveType);

    boolean isChildOf(Menu child, Menu parent);
}
