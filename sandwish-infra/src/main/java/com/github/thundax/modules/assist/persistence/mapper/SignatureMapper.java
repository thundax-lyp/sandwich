package com.github.thundax.modules.assist.persistence.mapper;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.assist.persistence.dataobject.SignatureDO;

/**
 * 签名 MyBatis Mapper。
 */
@MyBatisDao
public interface SignatureMapper extends CrudDao<SignatureDO> {

    int insertOrUpdate(SignatureDO entity);
}
