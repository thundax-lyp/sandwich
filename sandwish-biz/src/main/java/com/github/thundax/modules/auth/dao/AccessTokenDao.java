package com.github.thundax.modules.auth.dao;

import com.github.thundax.modules.auth.entity.AccessToken;

public interface AccessTokenDao {

    int getOnlineCount();

    String getUidByToken(String token);

    AccessToken getByUserId(String userId);

    void save(AccessToken accessToken);

    void active(AccessToken accessToken);

    void delete(AccessToken accessToken);
}
