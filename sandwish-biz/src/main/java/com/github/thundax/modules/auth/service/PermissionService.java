package com.github.thundax.modules.auth.service;

import com.github.thundax.modules.auth.entity.PermissionSession;


public interface PermissionService {

    PermissionSession createSession(String token, String userId);

    PermissionSession getSession(String token);

    void touch(String token);

    void release(String token);

    void reloadAll();

    boolean isPermitted(String token, String permission);
}
