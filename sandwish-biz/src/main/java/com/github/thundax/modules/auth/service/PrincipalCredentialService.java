package com.github.thundax.modules.auth.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import java.util.List;

public interface PrincipalCredentialService {

    @LayerPublicApi(reason = "统一认证主体资料维护时按 ID 读取凭据的业务入口")
    PrincipalCredential getById(EntityId id);

    @LayerPublicApi(reason = "统一认证主体登录时按标识和凭据类型读取凭据的业务入口")
    PrincipalCredential getByIdentityIdAndType(EntityId identityId, PrincipalCredentialType credentialType);

    @LayerPublicApi(reason = "统一认证主体资料维护时按主体和凭据类型读取凭据的业务入口")
    PrincipalCredential getByPrincipalKeyAndType(PrincipalKey principalKey, PrincipalCredentialType credentialType);

    @LayerPublicApi(reason = "统一认证主体资料维护时按主体和状态列出凭据的业务入口")
    List<PrincipalCredential> listByPrincipalKeyAndStatus(PrincipalKey principalKey, PrincipalCredentialStatus status);

    @LayerPublicApi(reason = "统一认证主体资料维护时新增凭据的业务入口")
    EntityId add(PrincipalCredential principalCredential);

    @LayerPublicApi(reason = "统一认证主体资料维护时更新凭据的业务入口")
    void update(PrincipalCredential principalCredential);

    @LayerPublicApi(reason = "统一认证主体资料维护时更新凭据状态的业务入口")
    void updateStatus(PrincipalCredential principalCredential);

    @LayerPublicApi(reason = "统一认证主体登录时更新凭据验证状态的业务入口")
    void updateVerifyState(PrincipalCredential principalCredential);
}
