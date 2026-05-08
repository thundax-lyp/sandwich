package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.modules.auth.dao.PreAuthSessionDao;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import org.springframework.stereotype.Service;

@Service
public class PreAuthSessionServiceImpl implements PreAuthSessionService {

    private final PreAuthSessionDao preAuthSessionDao;

    public PreAuthSessionServiceImpl(PreAuthSessionDao preAuthSessionDao) {
        this.preAuthSessionDao = preAuthSessionDao;
    }

    @Override
    public int count() {
        return preAuthSessionDao.count();
    }

    @Override
    public PreAuthSession create(int expiredSeconds) {
        PreAuthSession session = PreAuthSession.create(expiredSeconds);
        preAuthSessionDao.insert(session);
        return session;
    }

    @Override
    public PreAuthSessionId findIdByToken(PreAuthSessionToken token) {
        return preAuthSessionDao.getIdByToken(token);
    }

    @Override
    public PreAuthSessionId findIdByRefreshToken(PreAuthSessionToken refreshToken) {
        return preAuthSessionDao.getIdByRefreshToken(refreshToken);
    }

    @Override
    public PreAuthSession getById(PreAuthSessionId id) throws InvalidTokenException {
        PreAuthSession session = preAuthSessionDao.getById(id);
        if (session == null || session.isExpired()) {
            throw new InvalidTokenException();
        }
        return session;
    }

    @Override
    public PreAuthSession refresh(PreAuthSessionId id, int expiredSeconds, int refreshTokenGraceSeconds)
            throws InvalidTokenException {
        PreAuthSession session = getById(id);
        session.refresh(expiredSeconds, refreshTokenGraceSeconds);
        preAuthSessionDao.update(session);
        return session;
    }

    @Override
    public void release(PreAuthSessionId id) {
        preAuthSessionDao.deleteById(id);
    }

    @Override
    public void upsertValue(PreAuthSessionId id, String name, String value, long expiredAt)
            throws InvalidTokenException {
        PreAuthSession session = getById(id);
        session.upsertValue(name, value, expiredAt);
        preAuthSessionDao.update(session);
    }

    @Override
    public String findValue(PreAuthSessionId id, String name) throws InvalidTokenException {
        return getById(id).findValue(name);
    }
}
