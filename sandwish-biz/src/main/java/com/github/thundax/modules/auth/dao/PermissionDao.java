package com.github.thundax.modules.auth.dao;

import com.github.thundax.modules.auth.entity.PermissionSession;

public interface PermissionDao {

    PermissionSession getByToken(String token);

    void insert(PermissionSession session, int expiredSeconds);

    void touch(String token, int expiredSeconds);

    void deleteByToken(String token);

    void deleteAll();
}
