package com.github.thundax.modules.auth.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalIdentityId;
import com.github.thundax.modules.auth.service.command.PrincipalIdentityCommand;
import com.github.thundax.modules.auth.service.query.PrincipalIdentityQuery;
import java.util.List;

public interface PrincipalIdentityService {

    @LayerPublicApi(reason = "统一认证主体资料维护时按查询条件读取登录标识的业务入口")
    PrincipalIdentity get(PrincipalIdentityQuery query);

    @LayerPublicApi(reason = "统一认证主体资料维护时按查询条件列出登录标识的业务入口")
    List<PrincipalIdentity> list(PrincipalIdentityQuery query);

    @LayerPublicApi(reason = "统一认证主体资料维护时新增登录标识的业务入口")
    PrincipalIdentityId create(PrincipalIdentityCommand command);

    @LayerPublicApi(reason = "统一认证主体资料维护时更新登录标识的业务入口")
    void change(PrincipalIdentityCommand command);

    @LayerPublicApi(reason = "统一认证主体资料维护时启停登录标识的业务入口")
    void changeStatus(PrincipalIdentityCommand command);
}
