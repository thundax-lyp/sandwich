package com.github.thundax.modules.assist.dao;

/** 公私钥私钥临时存储 DAO。 */
public interface KeypairPrivateKeyDao {

    /**
     * 保存私钥。
     *
     * @param token 令牌
     * @param privateKey 私钥
     * @param expiredSeconds 过期秒数
     */
    void save(String token, String privateKey, int expiredSeconds);

    /**
     * 获取私钥。
     *
     * @param token 令牌
     * @return 私钥
     */
    String getByToken(String token);
}
