package com.github.thundax.modules.auth.dao;

import com.github.thundax.modules.auth.entity.PrincipalRefreshToken;
import com.github.thundax.modules.auth.entity.enums.PrincipalTokenStatus;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalRefreshTokenCode;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalRefreshTokenId;
import java.util.List;

public interface PrincipalRefreshTokenDao {

    PrincipalRefreshToken getById(PrincipalRefreshTokenId id);

    PrincipalRefreshToken getByTokenCode(PrincipalRefreshTokenCode tokenCode);

    PrincipalRefreshToken getByToken(String token);

    List<PrincipalRefreshToken> listByPrincipalKeyAndClientIdAndStatus(
            PrincipalKey principalKey, String clientId, PrincipalTokenStatus status);

    PrincipalRefreshTokenId insert(PrincipalRefreshToken refreshToken, String token);

    int updateStatus(PrincipalRefreshToken refreshToken);
}
