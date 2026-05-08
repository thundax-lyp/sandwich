package com.github.thundax.modules.auth.dao;

import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;

public interface PreAuthSessionDao {

    int count();

    PreAuthSession getById(PreAuthSessionId id);

    PreAuthSessionId getIdByToken(PreAuthSessionToken token);

    PreAuthSessionId getIdByRefreshToken(PreAuthSessionToken refreshToken);

    default PreAuthSession getByToken(PreAuthSessionToken token) {
        PreAuthSessionId id = getIdByToken(token);
        return id == null ? null : getById(id);
    }

    default PreAuthSession getByRefreshToken(PreAuthSessionToken refreshToken) {
        PreAuthSessionId id = getIdByRefreshToken(refreshToken);
        return id == null ? null : getById(id);
    }

    void insert(PreAuthSession session);

    void update(PreAuthSession session);

    void deleteById(PreAuthSessionId id);

    default void deleteByToken(PreAuthSessionToken token) {
        PreAuthSessionId id = getIdByToken(token);
        if (id != null) {
            deleteById(id);
        }
    }
}
