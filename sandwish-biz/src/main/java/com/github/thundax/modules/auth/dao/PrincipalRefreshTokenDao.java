package com.github.thundax.modules.auth.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.PrincipalRefreshToken;
import com.github.thundax.modules.auth.entity.enums.PrincipalTokenStatus;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import java.util.List;

public interface PrincipalRefreshTokenDao {

    PrincipalRefreshToken getById(EntityId id);

    PrincipalRefreshToken getByTokenId(String tokenId);

    PrincipalRefreshToken getByTokenHash(String tokenHash);

    List<PrincipalRefreshToken> listByPrincipalKeyAndClientIdAndStatus(
            PrincipalKey principalKey, String clientId, PrincipalTokenStatus status);

    EntityId insert(PrincipalRefreshToken refreshToken);

    int updateStatus(PrincipalRefreshToken refreshToken);
}
