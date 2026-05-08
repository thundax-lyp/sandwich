package com.github.thundax.modules.auth.dao;

import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.entity.enums.PrincipalTokenStatus;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalAccessTokenCode;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalAccessTokenId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import java.util.List;

public interface PrincipalAccessTokenDao {

    PrincipalAccessToken getById(PrincipalAccessTokenId id);

    PrincipalAccessToken getByTokenCode(PrincipalAccessTokenCode tokenCode);

    PrincipalAccessToken getByToken(String token);

    List<PrincipalAccessToken> listByPrincipalKeyAndClientIdAndStatus(
            PrincipalKey principalKey, String clientId, PrincipalTokenStatus status);

    int countByClientIdAndStatus(String clientId, PrincipalTokenStatus status);

    PrincipalAccessTokenId insert(PrincipalAccessToken accessToken, String token);

    int updateStatus(PrincipalAccessToken accessToken);
}
