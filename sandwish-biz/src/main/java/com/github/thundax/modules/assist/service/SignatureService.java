package com.github.thundax.modules.assist.service;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.assist.entity.Signature;
import java.util.List;

/**
 * 签名存储SERVICE
 *
 * @author wdit
 */
public interface SignatureService {

    /**
     * 通过业务分类和业务主键查找签名结果
     *
     * @param businessType 业务分类
     * @param businessId 业务主键
     * @return Signature 签名结果
     */
    Signature find(String businessType, String businessId);

    /**
     * 按业务分类分页查询签名结果。
     *
     * @param businessType 业务分类
     * @param page 分页对象
     * @return 分页结果
     */
    Page<Signature> findPage(String businessType, Page<Signature> page);

    /**
     * 新增签名结果。
     *
     * @param entity 签名结果
     */
    void add(Signature entity);

    /**
     * 更新签名结果。
     *
     * @param entity 签名结果
     */
    void update(Signature entity);

    /**
     * 删除签名结果。
     *
     * @param entity 签名结果
     * @return 变更数量
     */
    int delete(Signature entity);

    /**
     * 批量删除签名结果。
     *
     * @param list 签名结果列表
     * @return 变更数量
     */
    int delete(List<Signature> list);
}
