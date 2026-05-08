package com.github.thundax.modules.auth.service;

import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;

public interface PreAuthSessionService {

    int count();

    PreAuthSession create(int expiredSeconds);

    PreAuthSessionId findIdByToken(PreAuthSessionToken token);

    PreAuthSessionId findIdByRefreshToken(PreAuthSessionToken refreshToken);

    PreAuthSession getById(PreAuthSessionId id) throws InvalidTokenException;

    PreAuthSession refresh(PreAuthSessionId id, int expiredSeconds, int refreshTokenGraceSeconds)
            throws InvalidTokenException;

    void release(PreAuthSessionId id);

    void upsertValue(PreAuthSessionId id, String name, String value, long expiredAt) throws InvalidTokenException;

    String findValue(PreAuthSessionId id, String name) throws InvalidTokenException;
}
