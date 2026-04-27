package com.github.thundax.modules.assist.service;

/**
 * 签名服务
 *
 * @author wdit
 */
public interface SignService {

    /**
     * 签名
     *
     * @param businessType 业务分类
     * @param businessId 业务主键
     * @param body 待签名内容
     * @return true: success; false: fail; null: undo
     */
    Boolean sign(String businessType, String businessId, String body);

    /**
     * 签名验签
     *
     * @param businessType 业务分类
     * @param businessId 业务主键
     * @param body 待验签内容
     * @return true: success; false: fail; null: undo
     */
    Boolean verifySign(String businessType, String businessId, String body);

    /**
     * 删除签名
     *
     * @param businessType 业务分类
     * @param businessId 业务主键
     */
    void deleteSign(String businessType, String businessId);
}
