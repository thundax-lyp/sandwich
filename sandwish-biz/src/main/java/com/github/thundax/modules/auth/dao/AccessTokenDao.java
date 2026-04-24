package com.github.thundax.modules.auth.dao;

import com.github.thundax.modules.auth.entity.AccessToken;

/**
 * access token DAO。
 */
public interface AccessTokenDao {

    /**
     * 获取当前在线用户数
     *
     * @return 当前在线用户数
     */
    int getOnlineCount();

    /**
     * 获取 userId
     *
     * @param token token
     * @return AccessToken
     */
    String getUidByToken(String token);

    /**
     * 获取 AccessToken
     *
     * @param userId userId
     * @return AccessToken
     */
    AccessToken getByUserId(String userId);

    /**
     * 保存
     *
     * @param accessToken AccessToken
     */
    void save(AccessToken accessToken);

    /**
     * 激活
     *
     * @param accessToken AccessToken
     */
    void active(AccessToken accessToken);

    /**
     * 删除token
     *
     * @param accessToken accessToken
     */
    void delete(AccessToken accessToken);
}
