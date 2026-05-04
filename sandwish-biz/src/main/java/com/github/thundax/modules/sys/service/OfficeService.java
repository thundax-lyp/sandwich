package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.service.query.OfficeQuery;
import java.util.List;

public interface OfficeService {

    Office getById(EntityId id);

    List<Office> list(Office office);

    List<Office> list(OfficeQuery query);

    PageDTO<Office> page(OfficeQuery query, PageDTO<Office> page);

    void add(Office office);

    void update(Office office);

    int batchDeleteById(List<EntityId> ids);

    void moveTreeNode(Office fromBean, Office toBean, TreeNodeMoveType moveType);

    boolean isChildOf(Office child, Office parent);
}
