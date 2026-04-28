package com.github.thundax.modules.assist.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.assist.entity.Signature;
import java.util.List;

/**
 * 签名存储DAO
 *
 * @author wdit
 */
public interface SignatureDao {

    /**
     * 通过业务分类和业务主键查找签名。
     *
     * @param businessType 业务分类
     * @param businessId 业务主键
     * @return 签名数据
     */
    Signature find(String businessType, String businessId);

    /**
     * 按业务主键批量查询签名。
     *
     * @param businessIdList 业务主键列表
     * @return 签名列表
     */
    List<Signature> findByBusinessIds(List<String> businessIdList);

    /**
     * 按显式条件查询签名列表。
     *
     * @param businessType 业务分类
     * @param businessId 业务主键
     * @param businessIdList 业务主键列表
     * @param isVerifySign 验签状态
     * @return 签名列表
     */
    List<Signature> findList(String businessType, String businessId, List<String> businessIdList, String isVerifySign);

    /**
     * 按业务分类分页查询签名。
     *
     * @param businessType 业务分类
     * @param pageNo 页码
     * @param pageSize 分页大小
     * @return 分页结果
     */
    Page<Signature> findPage(String businessType, int pageNo, int pageSize);

    /**
     * 插入或更新
     *
     * @param entity 签名数据
     * @return 变更数量
     */
    int upsert(Signature entity);

    /**
     * 删除签名。
     *
     * @param businessType 业务分类
     * @param businessId 业务主键
     * @return 变更数量
     */
    int delete(String businessType, String businessId);
}
