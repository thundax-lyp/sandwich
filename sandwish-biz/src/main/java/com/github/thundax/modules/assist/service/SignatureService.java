package com.github.thundax.modules.assist.service;

import com.github.thundax.common.page.PageDTO;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.service.query.SignatureQuery;
import java.util.List;

/**
 * 签名存储SERVICE
 */
public interface SignatureService {

    /**
     * 通过业务分类和业务主键查找签名结果
     *
     * @param businessType 业务分类
     * @param businessId 业务主键
     * @return Signature 签名结果
     */
    Signature getByBusiness(String businessType, String businessId);

    PageDTO<Signature> page(SignatureQuery query, PageDTO<Signature> page);

    void add(Signature entity);

    void update(Signature entity);

    int deleteByBusiness(Signature entity);

    int batchDeleteByBusiness(List<Signature> list);
}
