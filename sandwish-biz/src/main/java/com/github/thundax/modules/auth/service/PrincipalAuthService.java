package com.github.thundax.modules.auth.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.service.command.AuthenticateIdentityCommand;
import com.github.thundax.modules.auth.service.command.AuthenticatePasswordCommand;

public interface PrincipalAuthService {

    @LayerPublicApi(reason = "统一认证主体非密码登录时解析登录标识的业务入口")
    PrincipalIdentity authenticateIdentity(AuthenticateIdentityCommand command) throws ApiException;

    PrincipalIdentity authenticatePassword(AuthenticatePasswordCommand command) throws ApiException;
}
