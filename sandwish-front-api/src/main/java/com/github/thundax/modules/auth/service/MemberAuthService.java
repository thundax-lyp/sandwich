package com.github.thundax.modules.auth.service;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.entity.enums.PrincipalAuthenticationMethod;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.service.result.MemberTokenResult;

public interface MemberAuthService {

    MemberTokenResult loginAccount(String account, String plainPassword) throws ApiException;

    default MemberTokenResult loginAccount(String account, String plainPassword, String ip, String userAgent)
            throws ApiException {
        return loginAccount(account, plainPassword);
    }

    MemberTokenResult loginSms(String mobile) throws ApiException;

    default MemberTokenResult loginSms(String mobile, String ip, String userAgent) throws ApiException {
        return loginSms(mobile);
    }

    MemberTokenResult refreshAccessToken(String refreshToken) throws ApiException;

    default MemberTokenResult refreshAccessToken(String refreshToken, String ip, String userAgent) throws ApiException {
        return refreshAccessToken(refreshToken);
    }

    void logout(String accessToken) throws ApiException;

    default void logout(String accessToken, String ip, String userAgent) throws ApiException {
        logout(accessToken);
    }

    PrincipalAccessToken getValidAccessToken(String accessToken);

    default void recordLoginFailed(
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            String ip,
            String userAgent,
            String reason) {}
}
