package com.github.thundax.modules.auth.service;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.TooManyLoginRequestException;
import com.github.thundax.modules.auth.exception.TooManyOnlineUserException;
import com.github.thundax.modules.auth.service.dto.PreAuthSessionDTO;

public interface PreAuthSessionService {

    PreAuthSessionDTO createPreAuthSession(PrincipalType principalType)
            throws TooManyLoginRequestException, TooManyOnlineUserException, ApiException;

    PreAuthSessionDTO refreshPreAuthSession(PrincipalType principalType, String refreshToken)
            throws InvalidTokenException, ApiException;

    void releasePreAuthSession(PrincipalType principalType, String loginToken);

    String createCaptcha(PrincipalType principalType, String loginToken) throws InvalidTokenException, ApiException;

    String getCaptcha(PrincipalType principalType, String loginToken)
            throws InvalidTokenException, InvalidCaptchaException, ApiException;

    boolean validateCaptcha(PrincipalType principalType, String loginToken, String captcha)
            throws InvalidTokenException, InvalidCaptchaException, ApiException;

    String createSmsValidateCode(PrincipalType principalType, String loginToken, String mobile)
            throws InvalidTokenException, ApiException;

    String getSmsValidateCode(PrincipalType principalType, String loginToken)
            throws InvalidTokenException, InvalidCaptchaException, ApiException;

    boolean validateSmsValidateCode(PrincipalType principalType, String loginToken, String mobile, String validateCode)
            throws InvalidTokenException, InvalidCaptchaException, ApiException;

    String createEmailValidateCode(PrincipalType principalType, String loginToken, String email) throws ApiException;

    boolean validateEmailValidateCode(PrincipalType principalType, String loginToken, String email, String validateCode)
            throws ApiException;

    String getPrivateKey(PrincipalType principalType, String loginToken) throws InvalidTokenException, ApiException;

    String decryptRsaValue(PrincipalType principalType, String loginToken, String encryptedValue) throws ApiException;
}
