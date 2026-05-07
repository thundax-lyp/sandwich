package com.github.thundax.modules.auth.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.modules.auth.entity.PermissionSession;

public interface PermissionService {

    PermissionSession createSession(String token, String userId);

    PermissionSession getSession(String token);

    void touch(String token);

    void release(String token);

    void reloadAll();

    @LayerPublicApi(reason = "权限认证适配层按令牌校验业务权限的稳定入口")
    boolean isPermitted(String token, String permission);
}
