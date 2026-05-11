package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.modules.auth.dao.PreAuthSessionDao;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.command.CreatePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.RefreshPreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.ReleasePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.UpsertPreAuthSessionValueCommand;
import com.github.thundax.modules.auth.service.query.PreAuthSessionQuery;
import com.github.thundax.modules.exception.BizExceptionBoundary;
import org.springframework.stereotype.Service;

@Service
public class PreAuthSessionServiceImpl implements PreAuthSessionService {

    private final PreAuthSessionDao preAuthSessionDao;

    public PreAuthSessionServiceImpl(PreAuthSessionDao preAuthSessionDao) {
        this.preAuthSessionDao = preAuthSessionDao;
    }

    @Override
    @BizExceptionBoundary
    public int count(PreAuthSessionQuery query) {
        return preAuthSessionDao.count();
    }

    @Override
    @BizExceptionBoundary
    public PreAuthSession create(CreatePreAuthSessionCommand command) {
        PreAuthSession session = PreAuthSession.create(command.getExpiredSeconds());
        preAuthSessionDao.insert(session);
        return session;
    }

    @Override
    @BizExceptionBoundary
    public PreAuthSessionId getIdByToken(PreAuthSessionQuery query) {
        return preAuthSessionDao.getByToken(query.getToken());
    }

    @Override
    @BizExceptionBoundary
    public PreAuthSessionId getIdByRefreshToken(PreAuthSessionQuery query) {
        return preAuthSessionDao.getByRefreshToken(query.getRefreshToken());
    }

    @Override
    @BizExceptionBoundary
    public PreAuthSession get(PreAuthSessionQuery query) throws InvalidTokenException {
        PreAuthSession session = preAuthSessionDao.getById(query.getId());
        if (session == null || session.isExpired()) {
            throw new InvalidTokenException();
        }
        return session;
    }

    @Override
    @BizExceptionBoundary
    public PreAuthSession refresh(RefreshPreAuthSessionCommand command) throws InvalidTokenException {
        PreAuthSessionQuery query = new PreAuthSessionQuery();
        query.setId(command.getId());
        PreAuthSession session = get(query);
        session.refresh(command.getExpiredSeconds(), command.getRefreshTokenGraceSeconds());
        preAuthSessionDao.update(session);
        return session;
    }

    @Override
    @BizExceptionBoundary
    public void release(ReleasePreAuthSessionCommand command) {
        preAuthSessionDao.deleteById(command.getId());
    }

    @Override
    @BizExceptionBoundary
    public void upsertValue(UpsertPreAuthSessionValueCommand command) throws InvalidTokenException {
        PreAuthSessionQuery query = new PreAuthSessionQuery();
        query.setId(command.getId());
        PreAuthSession session = get(query);
        session.upsertValue(command.getName(), command.getValue(), command.getExpiredAt());
        preAuthSessionDao.update(session);
    }

    @Override
    @BizExceptionBoundary
    public String getValue(PreAuthSessionQuery query) throws InvalidTokenException {
        return get(query).findValue(query.getName());
    }
}
