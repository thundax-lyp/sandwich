package com.github.thundax.modules.assist.service;

import com.github.thundax.common.service.CrudService;
import com.github.thundax.modules.assist.entity.Signature;

/**
 * 签名存储SERVICE
 *
 * @author wdit
 */
public interface SignatureService extends CrudService<Signature> {

    /**
     * 通过业务分类和业务主键查找签名结果
     *
     * @param businessType 业务分类
     * @param businessId   业务主键
     * @return Signature   签名结果
     */
    Signature find(String businessType, String businessId);

}
