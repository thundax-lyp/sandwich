package com.github.thundax.modules.assist.dao;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.modules.assist.entity.Signature;

/**
 * 签名存储DAO
 *
 * @author wdit
 */
public interface SignatureDao extends CrudDao<Signature> {

    /**
     * 插入或更新
     *
     * @param entity 签名数据
     * @return 变更数量
     */
    int insertOrUpdate(Signature entity);

}
