package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.BizException;
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
@BizExceptionBoundary
public class PreAuthSessionServiceImpl implements PreAuthSessionService {

    private final PreAuthSessionDao preAuthSessionDao;

    public PreAuthSessionServiceImpl(PreAuthSessionDao preAuthSessionDao) {
        this.preAuthSessionDao = preAuthSessionDao;
    }

    @Override
    public int count(PreAuthSessionQuery query) {
        return preAuthSessionDao.count();
    }

    @Override
    public PreAuthSession create(CreatePreAuthSessionCommand command) {
        PreAuthSession session = PreAuthSession.create(command.getExpiredSeconds());
        preAuthSessionDao.insert(session);
        return session;
    }

    @Override
    public PreAuthSessionId getIdByToken(PreAuthSessionQuery query) {
        return preAuthSessionDao.getByToken(query.getToken());
    }

    @Override
    public PreAuthSessionId getIdByRefreshToken(PreAuthSessionQuery query) {
        return preAuthSessionDao.getByRefreshToken(query.getRefreshToken());
    }

    @Override
    public PreAuthSession get(PreAuthSessionQuery query) {
        PreAuthSession session = preAuthSessionDao.getById(query.getId());
        if (session == null || session.isExpired()) {
            throw new BizException("AUTH-00006", "auth.exception.invalid-token", "token 已失效");
        }
        return session;
    }

    @Override
    public PreAuthSession refresh(RefreshPreAuthSessionCommand command) {
        PreAuthSessionQuery query = new PreAuthSessionQuery();
        query.setId(command.getId());
        PreAuthSession session = get(query);
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
        PreAuthSessionQuery query = new PreAuthSessionQuery();
        query.setId(command.getId());
        PreAuthSession session = get(query);
        session.upsertValue(command.getName(), command.getValue(), command.getExpiredAt());
        preAuthSessionDao.update(session);
    }

    @Override
    public String getValue(PreAuthSessionQuery query) {
        return get(query).findValue(query.getName());
    }
}
