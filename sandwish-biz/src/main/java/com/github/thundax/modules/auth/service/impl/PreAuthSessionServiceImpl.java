package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.id.UuidHelper;
import com.github.thundax.common.utils.RSAUtils;
import com.github.thundax.common.utils.encrypt.Sm2Helper;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.AccessTokenDao;
import com.github.thundax.modules.auth.dao.LoginFormDao;
import com.github.thundax.modules.auth.dao.MemberLoginFormDao;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.entity.MemberLoginForm;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.TooManyLoginRequestException;
import com.github.thundax.modules.auth.exception.TooManyOnlineUserException;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.dto.PreAuthSessionDTO;
import com.github.thundax.modules.auth.utils.AuthUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class PreAuthSessionServiceImpl implements PreAuthSessionService {

    private static final int CAPTCHA_LENGTH = 4;
    private static final int SMS_VALIDATE_CODE_LENGTH = 6;
    private static final int EMAIL_VALIDATE_CODE_LENGTH = 6;
    private static final String MEMBER_PRIVATE_KEY_SEPARATOR = ":";

    private static final char[] VALIDATE_CAPTCHA_CODE = {'2', '3', '4', '5', '6', '7', '8', '9'};

    private static final char[] VALIDATE_CODE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private final AuthProperties properties;
    private final LoginFormDao loginFormDao;
    private final AccessTokenDao accessTokenDao;
    private final MemberLoginFormDao memberLoginFormDao;

    public PreAuthSessionServiceImpl(
            AuthProperties properties,
            LoginFormDao loginFormDao,
            AccessTokenDao accessTokenDao,
            MemberLoginFormDao memberLoginFormDao) {
        this.properties = properties;
        this.loginFormDao = loginFormDao;
        this.accessTokenDao = accessTokenDao;
        this.memberLoginFormDao = memberLoginFormDao;
    }

    @Override
    public PreAuthSessionDTO createPreAuthSession(PrincipalType principalType)
            throws TooManyLoginRequestException, TooManyOnlineUserException, ApiException {
        if (principalType == PrincipalType.USER) {
            return createForUser();
        }
        if (principalType == PrincipalType.MEMBER) {
            return createForMember();
        }
        throw unsupportedPrincipalType();
    }

    @Override
    public PreAuthSessionDTO refreshPreAuthSession(PrincipalType principalType, String refreshToken)
            throws InvalidTokenException, ApiException {
        if (principalType == PrincipalType.USER) {
            return refreshForUser(refreshToken);
        }
        if (principalType == PrincipalType.MEMBER) {
            return refreshForMember(refreshToken);
        }
        throw unsupportedPrincipalType();
    }

    @Override
    public void releasePreAuthSession(PrincipalType principalType, String loginToken) {
        if (principalType == PrincipalType.USER) {
            loginFormDao.deleteByToken(loginToken);
            return;
        }
        if (principalType == PrincipalType.MEMBER) {
            memberLoginFormDao.deleteByToken(loginToken);
        }
    }

    @Override
    public String createCaptcha(PrincipalType principalType, String loginToken)
            throws InvalidTokenException, ApiException {
        String captcha = createCode(VALIDATE_CAPTCHA_CODE, CAPTCHA_LENGTH);
        if (principalType == PrincipalType.USER) {
            if (!loginFormDao.tokenExists(loginToken)) {
                throw new InvalidTokenException();
            }
            loginFormDao.updateCaptcha(loginToken, captcha);
            return captcha;
        }
        if (principalType == PrincipalType.MEMBER) {
            if (!memberLoginFormDao.tokenExists(loginToken)) {
                throw new ApiException("登录表单已失效");
            }
            memberLoginFormDao.updateCaptcha(loginToken, captcha);
            return captcha;
        }
        throw unsupportedPrincipalType();
    }

    @Override
    public String getCaptcha(PrincipalType principalType, String loginToken)
            throws InvalidTokenException, InvalidCaptchaException, ApiException {
        if (principalType == PrincipalType.USER) {
            LoginForm form = requireUserForm(loginToken);
            if (form.isNullCaptcha()) {
                throw new InvalidCaptchaException();
            }
            return form.getCaptcha();
        }
        if (principalType == PrincipalType.MEMBER) {
            MemberLoginForm form = requireMemberForm(loginToken);
            if (form.isNullCaptcha()) {
                throw new ApiException("图形验证码错误");
            }
            return form.getCaptcha();
        }
        throw unsupportedPrincipalType();
    }

    @Override
    public boolean validateCaptcha(PrincipalType principalType, String loginToken, String captcha)
            throws InvalidTokenException, InvalidCaptchaException, ApiException {
        if (StringUtils.isNotBlank(properties.getWhiteCaptcha())
                && StringUtils.equals(properties.getWhiteCaptcha(), captcha)) {
            return true;
        }
        return StringUtils.equals(captcha, getCaptcha(principalType, loginToken));
    }

    @Override
    public String createSmsValidateCode(PrincipalType principalType, String loginToken, String mobile)
            throws InvalidTokenException, ApiException {
        String validateCode = createCode(VALIDATE_CODE, SMS_VALIDATE_CODE_LENGTH);
        if (principalType == PrincipalType.USER) {
            if (!loginFormDao.tokenExists(loginToken)) {
                throw new InvalidTokenException();
            }
            loginFormDao.updateSmsValidateCode(loginToken, mobile, validateCode);
            loginFormDao.updateCaptcha(loginToken, null);
            return validateCode;
        }
        if (principalType == PrincipalType.MEMBER) {
            requireMemberForm(loginToken);
            memberLoginFormDao.updateSmsValidateCode(loginToken, mobile, validateCode);
            memberLoginFormDao.updateCaptcha(loginToken, null);
            return validateCode;
        }
        throw unsupportedPrincipalType();
    }

    @Override
    public String getSmsValidateCode(PrincipalType principalType, String loginToken)
            throws InvalidTokenException, InvalidCaptchaException, ApiException {
        if (principalType == PrincipalType.USER) {
            LoginForm form = requireUserForm(loginToken);
            if (StringUtils.isEmpty(form.getMobileValidateCode())) {
                throw new InvalidCaptchaException();
            }
            return form.getMobileValidateCode();
        }
        if (principalType == PrincipalType.MEMBER) {
            MemberLoginForm form = requireMemberForm(loginToken);
            if (StringUtils.isEmpty(form.getMobileValidateCode())) {
                throw new ApiException("短信验证码错误");
            }
            return form.getMobileValidateCode();
        }
        throw unsupportedPrincipalType();
    }

    @Override
    public boolean validateSmsValidateCode(
            PrincipalType principalType, String loginToken, String mobile, String validateCode)
            throws InvalidTokenException, InvalidCaptchaException, ApiException {
        if (StringUtils.isNotBlank(properties.getWhiteCaptcha())
                && StringUtils.equals(properties.getWhiteCaptcha(), validateCode)) {
            return true;
        }
        if (principalType == PrincipalType.USER) {
            LoginForm form = requireUserForm(loginToken);
            if (StringUtils.isEmpty(form.getMobile()) || StringUtils.isEmpty(form.getMobileValidateCode())) {
                throw new InvalidCaptchaException();
            }
            return StringUtils.equals(form.getMobile(), mobile)
                    && StringUtils.equals(form.getMobileValidateCode(), validateCode);
        }
        if (principalType == PrincipalType.MEMBER) {
            MemberLoginForm form = requireMemberForm(loginToken);
            return StringUtils.equals(form.getMobile(), mobile)
                    && StringUtils.equals(form.getMobileValidateCode(), validateCode);
        }
        throw unsupportedPrincipalType();
    }

    @Override
    public String createEmailValidateCode(PrincipalType principalType, String loginToken, String email)
            throws ApiException {
        if (principalType != PrincipalType.MEMBER) {
            throw unsupportedPrincipalType();
        }
        requireMemberForm(loginToken);
        String validateCode = createCode(VALIDATE_CODE, EMAIL_VALIDATE_CODE_LENGTH);
        memberLoginFormDao.updateEmailValidateCode(loginToken, email, validateCode);
        memberLoginFormDao.updateCaptcha(loginToken, null);
        return validateCode;
    }

    @Override
    public boolean validateEmailValidateCode(
            PrincipalType principalType, String loginToken, String email, String validateCode) throws ApiException {
        if (StringUtils.isNotBlank(properties.getWhiteCaptcha())
                && StringUtils.equals(properties.getWhiteCaptcha(), validateCode)) {
            return true;
        }
        if (principalType != PrincipalType.MEMBER) {
            throw unsupportedPrincipalType();
        }
        MemberLoginForm form = requireMemberForm(loginToken);
        return StringUtils.equals(form.getEmail(), email)
                && StringUtils.equals(form.getEmailValidateCode(), validateCode);
    }

    @Override
    public String getPrivateKey(PrincipalType principalType, String loginToken)
            throws InvalidTokenException, ApiException {
        if (principalType == PrincipalType.USER) {
            return requireUserForm(loginToken).getPrivateKey();
        }
        if (principalType == PrincipalType.MEMBER) {
            return requireMemberForm(loginToken).getPrivateKey();
        }
        throw unsupportedPrincipalType();
    }

    @Override
    public String decryptRsaValue(PrincipalType principalType, String loginToken, String encryptedValue)
            throws ApiException {
        if (principalType != PrincipalType.MEMBER) {
            throw unsupportedPrincipalType();
        }
        MemberLoginForm form = requireMemberForm(loginToken);
        if (StringUtils.isBlank(form.getPrivateKey())) {
            throw new ApiException("登录表单密钥已失效");
        }
        String[] privateKeyParts = StringUtils.split(form.getPrivateKey(), MEMBER_PRIVATE_KEY_SEPARATOR);
        if (privateKeyParts == null || privateKeyParts.length != 2) {
            throw new ApiException("登录表单密钥已失效");
        }
        RSAUtils.ReadableKeyPair keyPair =
                new RSAUtils.ReadableKeyPair(null, privateKeyParts[0], null, privateKeyParts[1]);
        return RSAUtils.decryptBase64(encryptedValue, keyPair);
    }

    private PreAuthSessionDTO createForUser() throws TooManyLoginRequestException, TooManyOnlineUserException {
        if (loginFormDao.count() > properties.getMaxLoginCount()) {
            throw new TooManyLoginRequestException();
        }
        if (accessTokenDao.count() > properties.getMaxOnlineCount()) {
            throw new TooManyOnlineUserException();
        }

        LoginForm form = new LoginForm();
        form.setLoginToken(UuidHelper.compact());
        form.setRefreshTokenList(new ArrayList<>(Collections.singletonList(UuidHelper.compact())));
        form.setExpiredSeconds(properties.getLoginExpiredSeconds());
        form.setCheckCode(AuthUtils.currentCheckCode());
        form.setCaptcha(createCode(VALIDATE_CAPTCHA_CODE, CAPTCHA_LENGTH));

        Sm2Helper.StringKeyPair keyPair = Sm2Helper.generateKeyPair();
        if (keyPair != null) {
            form.setPublicKey(keyPair.getPublicKey());
            form.setPrivateKey(keyPair.getPrivateKey());
        }

        loginFormDao.insert(form);
        return toPreAuthSessionDTO(form);
    }

    private PreAuthSessionDTO createForMember() throws ApiException {
        if (memberLoginFormDao.count() > properties.getMaxLoginCount()) {
            throw new TooManyLoginRequestException();
        }

        MemberLoginForm form = new MemberLoginForm();
        form.setLoginToken(UuidHelper.compact());
        form.setRefreshTokenList(new ArrayList<>(Collections.singletonList(UuidHelper.compact())));
        form.setExpiredSeconds(properties.getLoginExpiredSeconds());
        form.setCheckCode(AuthUtils.currentCheckCode());
        form.setCaptcha(createCode(VALIDATE_CAPTCHA_CODE, CAPTCHA_LENGTH));
        RSAUtils.ReadableKeyPair keyPair = RSAUtils.generateKeyPair();
        form.setPublicKey(keyPair.getPublicKey());
        form.setPrivateKey(memberPrivateKeyValue(keyPair));
        memberLoginFormDao.insert(form);
        return toPreAuthSessionDTO(form);
    }

    private PreAuthSessionDTO refreshForUser(String refreshToken) throws InvalidTokenException {
        LoginForm form = loginFormDao.getByRefreshToken(refreshToken);
        if (form == null || !form.validateCheckCode()) {
            throw new InvalidTokenException();
        }
        List<String> refreshTokenList = new ArrayList<>(form.getRefreshTokenList());
        refreshTokenList.add(0, UuidHelper.compact());
        form.setLoginToken(UuidHelper.compact());
        form.setRefreshTokenList(refreshTokenList);
        form.setExpiredSeconds(properties.getLoginExpiredSeconds());
        form.setCheckCode(AuthUtils.currentCheckCode());
        loginFormDao.insert(form);
        return toPreAuthSessionDTO(form);
    }

    private PreAuthSessionDTO refreshForMember(String refreshToken) throws ApiException {
        MemberLoginForm form = memberLoginFormDao.getByRefreshToken(refreshToken);
        if (form == null || !form.validateCheckCode()) {
            throw new ApiException("登录表单已失效");
        }
        form.getRefreshTokenList().add(0, UuidHelper.compact());
        form.setLoginToken(UuidHelper.compact());
        form.setExpiredSeconds(properties.getLoginExpiredSeconds());
        form.setCheckCode(AuthUtils.currentCheckCode());
        memberLoginFormDao.insert(form);
        return toPreAuthSessionDTO(form);
    }

    private LoginForm requireUserForm(String loginToken) throws InvalidTokenException {
        LoginForm form = loginFormDao.getByToken(loginToken);
        if (form == null || !form.validateCheckCode()) {
            throw new InvalidTokenException();
        }
        return form;
    }

    private MemberLoginForm requireMemberForm(String loginToken) throws ApiException {
        if (StringUtils.isBlank(loginToken)) {
            throw new ApiException("登录表单已失效");
        }
        MemberLoginForm form = memberLoginFormDao.getByToken(loginToken);
        if (form == null || !form.validateCheckCode()) {
            throw new ApiException("登录表单已失效");
        }
        return form;
    }

    private String memberPrivateKeyValue(RSAUtils.ReadableKeyPair keyPair) {
        return keyPair.getModulus() + MEMBER_PRIVATE_KEY_SEPARATOR + keyPair.getPrivateKeyExponent();
    }

    private PreAuthSessionDTO toPreAuthSessionDTO(LoginForm form) {
        PreAuthSessionDTO session = new PreAuthSessionDTO();
        session.setLoginToken(form.getLoginToken());
        session.setRefreshTokenList(form.getRefreshTokenList());
        session.setExpiredSeconds(form.getExpiredSeconds());
        session.setPublicKey(form.getPublicKey());
        return session;
    }

    private PreAuthSessionDTO toPreAuthSessionDTO(MemberLoginForm form) {
        PreAuthSessionDTO session = new PreAuthSessionDTO();
        session.setLoginToken(form.getLoginToken());
        session.setRefreshTokenList(form.getRefreshTokenList());
        session.setExpiredSeconds(form.getExpiredSeconds());
        session.setPublicKey(form.getPublicKey());
        return session;
    }

    private ApiException unsupportedPrincipalType() {
        return new ApiException("不支持的登录主体类型");
    }

    private String createCode(char[] validateChars, int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < length; idx++) {
            sb.append(validateChars[random.nextInt(validateChars.length)]);
        }
        return sb.toString();
    }
}
