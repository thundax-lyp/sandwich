package com.github.thundax.modules.assist.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.assist.entity.Signature;
import java.util.List;

/**
 * 签名存储DAO
 */
public interface SignatureDao {

    /**
     * 通过业务分类和业务主键查找签名。
     *
     * @param businessType 业务分类
     * @param businessId 业务主键
     * @return 签名数据
     */
    Signature getByBusiness(String businessType, String businessId);

    /**
     * 按业务主键批量查询签名。
     *
     * @param businessIdList 业务主键列表
     * @return 签名列表
     */
    List<Signature> batchGetByBusinessIds(List<String> businessIdList);

    /**
     * 按显式条件查询签名列表。
     *
     * @param businessType 业务分类
     * @param businessId 业务主键
     * @param businessIdList 业务主键列表
     * @param isVerifySign 验签状态
     * @return 签名列表
     */
    List<Signature> list(String businessType, String businessId, List<String> businessIdList, String isVerifySign);

    Page<Signature> page(String businessType, int pageNo, int pageSize);

    String insert(Signature entity);

    int update(Signature entity);

    int deleteByBusiness(String businessType, String businessId);
}
