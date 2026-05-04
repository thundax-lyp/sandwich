package com.github.thundax.modules.auth.dao;

import com.github.thundax.modules.auth.entity.AuthSession;
import java.util.Date;

public interface AuthSessionRuntimeDao {

    AuthSession getByToken(String token);

    void insert(AuthSession authSession, int expiredSeconds);

    void touch(String token, Date accessTime, int expiredSeconds);

    void deleteByToken(String token);
}
