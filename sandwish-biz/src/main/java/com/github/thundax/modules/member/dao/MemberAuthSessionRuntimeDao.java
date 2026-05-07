package com.github.thundax.modules.member.dao;

import com.github.thundax.modules.member.entity.MemberAuthSession;

public interface MemberAuthSessionRuntimeDao {

    MemberAuthSession getBySessionId(String sessionId);

    void insert(MemberAuthSession authSession, int expireSeconds);

    void deleteBySessionId(String sessionId);
}
