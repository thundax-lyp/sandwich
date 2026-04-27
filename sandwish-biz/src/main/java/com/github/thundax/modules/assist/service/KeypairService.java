package com.github.thundax.modules.assist.service;

/** 公私钥对服务。 */
public interface KeypairService {

    /**
     * 创建公钥并临时保存私钥。
     *
     * @param token 令牌
     * @return 公钥
     */
    String createPublicKey(String token);

    /**
     * 获取私钥。
     *
     * @param token 令牌
     * @return 私钥
     */
    String getPrivateKey(String token);
}
