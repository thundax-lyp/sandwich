package com.github.thundax.modules.auth.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.AuthSession;
import com.github.thundax.modules.auth.entity.enums.AuthSessionStatus;
import java.util.List;

public interface AuthSessionDao {

    AuthSession getById(EntityId id);

    AuthSession getBySessionId(String sessionId);

    AuthSession getByToken(String token);

    List<AuthSession> listByUserIdAndStatus(EntityId userId, AuthSessionStatus status);

    String insert(AuthSession authSession);

    int updateAccessTime(AuthSession authSession);

    int updateLogout(AuthSession authSession);

    int updateInvalidate(AuthSession authSession);

    int updateExpire(AuthSession authSession);
}
