package com.github.thundax.modules.auth.service;

/**
 * PasswordService
 *
 * @author wdit
 */
public interface PasswordService {

    /**
     * 加密
     *
     * @param plainPassword 原始密码
     * @return 密文
     */
    String encrypt(String plainPassword);

    /**
     * 校验
     *
     * @param plainPassword 原始密码
     * @param encryptedPassword 密文
     * @return 正确:true, 其他:false
     */
    boolean validate(String plainPassword, String encryptedPassword);
}
