package com.github.thundax.modules.auth.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalCredentialId;
import com.github.thundax.modules.auth.service.command.PrincipalCredentialCommand;
import com.github.thundax.modules.auth.service.query.PrincipalCredentialQuery;
import java.util.List;

public interface PrincipalCredentialService {

    @LayerPublicApi(reason = "统一认证主体资料维护时按查询条件读取凭据的业务入口")
    PrincipalCredential get(PrincipalCredentialQuery query);

    @LayerPublicApi(reason = "统一认证主体资料维护时按查询条件列出凭据的业务入口")
    List<PrincipalCredential> list(PrincipalCredentialQuery query);

    @LayerPublicApi(reason = "统一认证主体资料维护时新增凭据的业务入口")
    PrincipalCredentialId create(PrincipalCredentialCommand command);

    @LayerPublicApi(reason = "统一认证主体资料维护时更新凭据的业务入口")
    void change(PrincipalCredentialCommand command);

    @LayerPublicApi(reason = "统一认证主体资料维护时更新凭据状态的业务入口")
    void changeStatus(PrincipalCredentialCommand command);

    @LayerPublicApi(reason = "统一认证主体登录时更新凭据验证状态的业务入口")
    void changeVerifyState(PrincipalCredentialCommand command);
}
