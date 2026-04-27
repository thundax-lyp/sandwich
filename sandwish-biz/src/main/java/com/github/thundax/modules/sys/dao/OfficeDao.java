package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.modules.sys.entity.Office;

/** @author wdit */
public interface OfficeDao extends CrudDao<Office> {

    void moveTreeNode(String fromId, String toId, TreeService.MoveTreeNodeType moveType);

    boolean isChildOf(String childId, String parentId);
}
