package com.github.thundax.modules.auth.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.MemberAuthSession;

public interface MemberAuthSessionRuntimeDao {

    MemberAuthSession getById(EntityId id);

    void insert(MemberAuthSession authSession, int expireSeconds);

    void deleteById(EntityId id);
}
