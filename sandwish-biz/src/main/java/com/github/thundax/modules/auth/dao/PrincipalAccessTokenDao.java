package com.github.thundax.modules.auth.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalTokenStatus;
import java.util.List;

public interface PrincipalAccessTokenDao {

    PrincipalAccessToken getById(EntityId id);

    PrincipalAccessToken getByTokenId(String tokenId);

    PrincipalAccessToken getByTokenHash(String tokenHash);

    List<PrincipalAccessToken> listByPrincipalKeyAndClientIdAndStatus(
            PrincipalKey principalKey, String clientId, PrincipalTokenStatus status);

    EntityId insert(PrincipalAccessToken accessToken);

    int updateStatus(PrincipalAccessToken accessToken);
}
