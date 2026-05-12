package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.BizException;
import com.github.thundax.modules.auth.dao.PreAuthSessionDao;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.command.CreatePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.RefreshPreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.ReleasePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.UpsertPreAuthSessionValueCommand;
import com.github.thundax.modules.exception.BizExceptionBoundary;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
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
    public PreAuthSession create(CreatePreAuthSessionCommand command) {
        PreAuthSession session = PreAuthSession.create(command.getExpiredSeconds());
        preAuthSessionDao.insert(session);
        return session;
    }

    @Override
    public PreAuthSessionId getIdByToken(PreAuthSessionToken token) {
        return preAuthSessionDao.getByToken(token);
    }

    @Override
    public PreAuthSessionId getIdByRefreshToken(PreAuthSessionToken refreshToken) {
        return preAuthSessionDao.getByRefreshToken(refreshToken);
    }

    @Override
    public PreAuthSession get(PreAuthSessionId id) {
        PreAuthSession session = preAuthSessionDao.getById(id);
        if (session == null || session.isExpired()) {
            throw new BizException("AUTH-00006", "auth.exception.invalid-token", "token 已失效");
        }
        return session;
    }

    @Override
    public PreAuthSession refresh(RefreshPreAuthSessionCommand command) {
        PreAuthSession session = get(command.getId());
        session.refresh(command.getExpiredSeconds(), command.getRefreshTokenGraceSeconds());
        preAuthSessionDao.update(session);
        return session;
    }

    @Override
    public void release(ReleasePreAuthSessionCommand command) {
        preAuthSessionDao.deleteById(command.getId());
    }

    @Override
    public void upsertValue(UpsertPreAuthSessionValueCommand command) {
        PreAuthSession session = get(command.getId());
        session.upsertValue(command.getName(), command.getValue(), command.getExpiredAt());
        preAuthSessionDao.update(session);
    }

    @Override
    public String getValue(PreAuthSessionId id, String name) {
        return get(id).findValue(name);
    }
}
