package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.persistence.TreeDao;
import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.entity.Office;

/**
 * @author wdit
 */
@MyBatisDao
public interface OfficeDao extends TreeDao<Office> {

}
