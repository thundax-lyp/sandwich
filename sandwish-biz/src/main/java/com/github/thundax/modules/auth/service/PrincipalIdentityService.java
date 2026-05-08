package com.github.thundax.modules.auth.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import java.util.List;

public interface PrincipalIdentityService {

    @LayerPublicApi(reason = "统一认证主体资料维护时按 ID 读取登录标识的业务入口")
    PrincipalIdentity getById(EntityId id);

    @LayerPublicApi(reason = "统一认证主体登录时按标识读取身份的业务入口")
    PrincipalIdentity getByIdentity(PrincipalIdentityType identityType, String identityValue);

    @LayerPublicApi(reason = "统一认证主体资料维护时按主体和标识类型读取身份的业务入口")
    PrincipalIdentity getByPrincipalKeyAndType(PrincipalKey principalKey, PrincipalIdentityType identityType);

    @LayerPublicApi(reason = "统一认证主体资料维护时按主体和状态列出身份的业务入口")
    List<PrincipalIdentity> listByPrincipalKeyAndStatus(PrincipalKey principalKey, PrincipalIdentityStatus status);

    @LayerPublicApi(reason = "统一认证主体资料维护时新增登录标识的业务入口")
    EntityId add(PrincipalIdentity principalIdentity);

    @LayerPublicApi(reason = "统一认证主体资料维护时更新登录标识的业务入口")
    void update(PrincipalIdentity principalIdentity);

    @LayerPublicApi(reason = "统一认证主体资料维护时启停登录标识的业务入口")
    void updateStatus(PrincipalIdentity principalIdentity);
}
