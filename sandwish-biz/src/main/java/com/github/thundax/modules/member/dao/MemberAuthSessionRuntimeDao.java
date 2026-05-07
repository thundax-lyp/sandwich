package com.github.thundax.modules.member.dao;

import com.github.thundax.modules.member.entity.MemberAuthSession;

public interface MemberAuthSessionRuntimeDao {

    MemberAuthSession getBySessionId(String sessionId);

    void put(MemberAuthSession authSession, int expireSeconds);

    void remove(String sessionId);
}
