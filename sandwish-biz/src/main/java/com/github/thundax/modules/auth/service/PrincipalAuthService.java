package com.github.thundax.modules.auth.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.service.dto.PrincipalPasswordPolicyDTO;

public interface PrincipalAuthService {

    @LayerPublicApi(reason = "统一认证主体非密码登录时解析登录标识的业务入口")
    PrincipalIdentity authenticateIdentity(PrincipalIdentityType identityType, String identityValue)
            throws ApiException;

    PrincipalIdentity authenticatePassword(
            PrincipalIdentityType identityType,
            String identityValue,
            PrincipalCredentialType credentialType,
            String plainPassword,
            PrincipalPasswordPolicyDTO passwordPolicy)
            throws ApiException;
}
