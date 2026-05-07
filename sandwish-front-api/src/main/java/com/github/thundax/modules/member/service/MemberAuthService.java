package com.github.thundax.modules.member.service;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.member.entity.MemberAccessToken;
import com.github.thundax.modules.member.entity.MemberLoginForm;
import com.github.thundax.modules.member.service.result.MemberTokenResult;

public interface MemberAuthService {

    MemberLoginForm createLoginForm() throws ApiException;

    MemberLoginForm refreshLoginForm(String refreshToken) throws ApiException;

    String createCaptcha(String loginToken) throws ApiException;

    MemberTokenResult loginAccount(String loginToken, String account, String encryptedPassword, String captcha)
            throws ApiException;

    MemberTokenResult loginSms(String loginToken, String mobile, String validateCode) throws ApiException;

    MemberTokenResult refreshAccessToken(String refreshToken) throws ApiException;

    void logout(String accessToken) throws ApiException;

    MemberAccessToken getValidAccessToken(String accessToken);
}
