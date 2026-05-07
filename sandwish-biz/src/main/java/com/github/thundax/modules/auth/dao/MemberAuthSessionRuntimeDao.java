package com.github.thundax.modules.auth.dao;

import com.github.thundax.modules.auth.entity.MemberAuthSession;

public interface MemberAuthSessionRuntimeDao {

    MemberAuthSession getBySessionId(String sessionId);

    void insert(MemberAuthSession authSession, int expireSeconds);

    void deleteBySessionId(String sessionId);
}
