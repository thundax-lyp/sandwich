package com.github.thundax.modules.assist.service;

/**
 * 国密服务
 *
 * @author wdit
 */
public interface EncryptService {

    /**
     * 加密
     *
     * @param value 被加密数据
     * @return 加密结果
     */
    String encrypt(String value);

    /**
     * 解密
     *
     * @param encryptedValue 被解密数据
     * @return 解密结果
     */
    String decrypt(String encryptedValue);
}
