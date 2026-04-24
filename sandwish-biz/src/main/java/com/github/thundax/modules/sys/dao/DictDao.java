package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.entity.Dict;

import java.util.List;

/**
 * @author wdit
 */
@MyBatisDao
public interface DictDao extends CrudDao<Dict> {

    /**
     * 获取类型列表
     *
     * @return 类型列表
     */
    List<String> findTypeList();






}
