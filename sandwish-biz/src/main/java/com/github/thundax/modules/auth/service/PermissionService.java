package com.github.thundax.modules.auth.service;

import com.github.thundax.modules.auth.entity.PermissionSession;

/** 后台权限会话服务。 */
public interface PermissionService {

    PermissionSession createSession(String token, String userId);

    PermissionSession getSession(String token);

    void touch(String token);

    void release(String token);

    void reloadAll();

    boolean isPermitted(String token, String permission);
}
