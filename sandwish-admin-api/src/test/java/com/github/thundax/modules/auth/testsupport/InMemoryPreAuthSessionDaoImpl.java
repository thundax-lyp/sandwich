package com.github.thundax.modules.auth.testsupport;

import com.github.thundax.modules.auth.dao.PreAuthSessionDao;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.PreAuthSession.RefreshTokenValue;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import java.util.LinkedHashMap;
import java.util.Map;

public class InMemoryPreAuthSessionDaoImpl implements PreAuthSessionDao {
    private final Map<String, PreAuthSession> sessions = new LinkedHashMap<>();
    private final Map<String, String> tokenIndex = new LinkedHashMap<>();
    private final Map<String, String> refreshTokenIndex = new LinkedHashMap<>();

    @Override
    public int count() {
        return sessions.size();
    }

    @Override
    public PreAuthSession getById(PreAuthSessionId id) {
        return id == null ? null : sessions.get(id.asString());
    }

    @Override
    public PreAuthSessionId getByToken(PreAuthSessionToken token) {
        return token == null ? null : PreAuthSessionId.ofNullable(tokenIndex.get(token.asString()));
    }

    @Override
    public PreAuthSessionId getByRefreshToken(PreAuthSessionToken refreshToken) {
        return refreshToken == null
                ? null
                : PreAuthSessionId.ofNullable(refreshTokenIndex.get(refreshToken.asString()));
    }

    @Override
    public void insert(PreAuthSession session) {
        put(session);
    }

    @Override
    public void update(PreAuthSession session) {
        removeIndexesBySessionId(session.getId());
        put(session);
    }

    @Override
    public void deleteById(PreAuthSessionId id) {
        PreAuthSession session = sessions.remove(id.asString());
        if (session != null) {
            removeIndexesBySessionId(id);
        }
    }

    private void put(PreAuthSession session) {
        sessions.put(session.getId().asString(), session);
        tokenIndex.put(session.getToken().asString(), session.getId().asString());
        long now = System.currentTimeMillis();
        for (RefreshTokenValue refreshToken : session.refreshTokenValues()) {
            if (refreshToken.getExpiredAt() > now) {
                refreshTokenIndex.put(
                        refreshToken.getToken().asString(), session.getId().asString());
            }
        }
    }

    private void removeIndexesBySessionId(PreAuthSessionId id) {
        tokenIndex.entrySet().removeIf(entry -> id.asString().equals(entry.getValue()));
        refreshTokenIndex.entrySet().removeIf(entry -> id.asString().equals(entry.getValue()));
    }
}
