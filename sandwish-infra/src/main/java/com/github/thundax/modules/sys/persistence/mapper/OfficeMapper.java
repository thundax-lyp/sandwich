package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.TreeDao;
import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.OfficeDO;

/**
 * 机构 MyBatis Mapper。
 */
@MyBatisDao
public interface OfficeMapper extends TreeDao<OfficeDO> {
}
