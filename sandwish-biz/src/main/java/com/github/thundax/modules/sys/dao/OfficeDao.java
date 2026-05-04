package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.modules.sys.entity.Office;
import java.util.List;

public interface OfficeDao {

    Office getById(EntityId id);

    List<Office> batchGetByIds(List<String> idList);

    List<Office> list(String parentId, String name, String remarks);

    Page<Office> page(String parentId, String name, String remarks, int pageNo, int pageSize);

    String insert(Office office);

    int update(Office office);

    int updatePriority(Office office);

    int deleteById(EntityId id);

    void moveTreeNode(String fromId, String toId, TreeNodeMoveType moveType);

    boolean isChildOf(String childId, String parentId);
}
