package com.github.thundax.modules.auth.service;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.service.result.MemberTokenResult;

public interface MemberAuthService {

    MemberTokenResult loginAccount(String account, String plainPassword) throws ApiException;

    MemberTokenResult loginSms(String mobile) throws ApiException;

    MemberTokenResult refreshAccessToken(String refreshToken) throws ApiException;

    void logout(String accessToken) throws ApiException;

    PrincipalAccessToken getValidAccessToken(String accessToken);
}
