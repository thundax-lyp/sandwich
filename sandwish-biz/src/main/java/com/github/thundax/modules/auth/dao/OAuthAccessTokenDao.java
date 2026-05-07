package com.github.thundax.modules.auth.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.OAuthAccessToken;

public interface OAuthAccessTokenDao {

    OAuthAccessToken getById(EntityId id);

    OAuthAccessToken getByTokenId(String tokenId);

    OAuthAccessToken getByTokenHash(String tokenHash);

    EntityId insert(OAuthAccessToken accessToken);

    int updateStatus(OAuthAccessToken accessToken);
}
