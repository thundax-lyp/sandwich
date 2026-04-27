package com.github.thundax.modules.auth.dao;

import com.github.thundax.modules.auth.entity.PermissionSession;

/**
 * 后台权限会话 DAO。
 */
public interface PermissionDao {

    PermissionSession getByToken(String token);

    void save(PermissionSession session, int expiredSeconds);

    void touch(String token, int expiredSeconds);

    void deleteByToken(String token);

    void deleteAll();
}
