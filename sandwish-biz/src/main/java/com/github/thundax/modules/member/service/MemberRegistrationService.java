package com.github.thundax.modules.member.service;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityId;

public interface MemberRegistrationService {

    EntityId registerAccount(String loginToken, String name, String account, String encryptedPassword, String captcha)
            throws ApiException;

    void sendRegisterSmsCode(String loginToken, String mobile, String captcha) throws ApiException;

    EntityId registerMobile(String loginToken, String name, String mobile, String validateCode) throws ApiException;

    void sendRegisterEmailCode(String loginToken, String email, String captcha) throws ApiException;

    EntityId registerEmail(String loginToken, String name, String email, String validateCode) throws ApiException;
}
