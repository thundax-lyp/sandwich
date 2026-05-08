package com.github.thundax.modules.auth.service;

import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.TooManyLoginRequestException;
import com.github.thundax.modules.auth.exception.TooManyOnlineUserException;

public interface PreAuthSessionService {

    LoginForm createLoginForm() throws TooManyLoginRequestException, TooManyOnlineUserException;

    LoginForm refreshLoginForm(String refreshToken) throws InvalidTokenException;

    void deleteLoginForm(String loginToken);

    String createCaptcha(String loginToken) throws InvalidTokenException;

    String getCaptcha(String loginToken) throws InvalidTokenException, InvalidCaptchaException;

    boolean validateCaptcha(String loginToken, String captcha) throws InvalidTokenException, InvalidCaptchaException;

    String createSmsValidateCode(String loginToken, String mobile) throws InvalidTokenException;

    String getSmsValidateCode(String loginToken) throws InvalidTokenException, InvalidCaptchaException;

    boolean validateSmsValidateCode(String loginToken, String mobile, String validateCode)
            throws InvalidTokenException, InvalidCaptchaException;

    String getPrivateKey(String loginToken) throws InvalidTokenException;
}
