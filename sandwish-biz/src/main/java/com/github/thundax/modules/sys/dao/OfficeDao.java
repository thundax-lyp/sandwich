package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.modules.sys.entity.Office;

/**
 * @author wdit
 */
public interface OfficeDao extends CrudDao<Office> {

    Office getTreeNode(String id);

    void updateLftRgt(Office node);

    void updateParent(Office node);

    Integer getMaxPosition();

    void moveTreeRgts(Integer from, Integer offset);

    void moveTreeLfts(Integer from, Integer offset);

    void moveTreeNodes(Integer from, Integer to, Integer offset);

}
