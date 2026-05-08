package com.github.thundax.modules.auth.dao;

import com.github.thundax.modules.auth.entity.PrincipalAuthSession;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalAuthSessionId;
import java.util.Date;

public interface PrincipalAuthSessionDao {

    PrincipalAuthSession getById(PrincipalAuthSessionId id);

    void insert(PrincipalAuthSession session, int expireSeconds);

    void touch(PrincipalAuthSessionId id, Date accessTime, int expireSeconds);

    void deleteById(PrincipalAuthSessionId id);
}
