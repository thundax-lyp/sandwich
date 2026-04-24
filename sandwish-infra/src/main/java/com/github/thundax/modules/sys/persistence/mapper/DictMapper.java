package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.DictDO;

import java.util.List;

/**
 * 字典 MyBatis Mapper。
 */
@MyBatisDao
public interface DictMapper extends CrudDao<DictDO> {

    /**
     * 获取类型列表。
     *
     * @return 类型列表
     */
    List<String> findTypeList();
}
