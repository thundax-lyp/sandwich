package com.github.thundax.modules.auth.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.OAuthRefreshToken;
import com.github.thundax.modules.auth.entity.enums.OAuthRefreshTokenStatus;
import java.util.List;

public interface OAuthRefreshTokenDao {

    OAuthRefreshToken getById(EntityId id);

    OAuthRefreshToken getByTokenId(String tokenId);

    OAuthRefreshToken getByTokenHash(String tokenHash);

    List<OAuthRefreshToken> listByClientIdAndUserIdAndStatus(
            String clientId, EntityId userId, OAuthRefreshTokenStatus status);

    EntityId insert(OAuthRefreshToken refreshToken);

    int updateStatus(OAuthRefreshToken refreshToken);
}
