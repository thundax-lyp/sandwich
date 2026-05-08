package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.id.UuidHelper;
import com.github.thundax.common.utils.encrypt.Sm2Helper;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.AccessTokenDao;
import com.github.thundax.modules.auth.dao.LoginFormDao;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.TooManyLoginRequestException;
import com.github.thundax.modules.auth.exception.TooManyOnlineUserException;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
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

    private static final char[] VALIDATE_CAPTCHA_CODE = {'2', '3', '4', '5', '6', '7', '8', '9'};

    private static final char[] VALIDATE_SMS_VALIDATE_CODE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private final AuthProperties properties;
    private final LoginFormDao loginFormDao;
    private final AccessTokenDao accessTokenDao;

    public PreAuthSessionServiceImpl(
            AuthProperties properties, LoginFormDao loginFormDao, AccessTokenDao accessTokenDao) {
        this.properties = properties;
        this.loginFormDao = loginFormDao;
        this.accessTokenDao = accessTokenDao;
    }

    @Override
    public LoginForm createLoginForm() throws TooManyLoginRequestException, TooManyOnlineUserException {
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

        return form;
    }

    @Override
    public LoginForm refreshLoginForm(String refreshToken) throws InvalidTokenException {
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

        return form;
    }

    @Override
    public void deleteLoginForm(String loginToken) {
        loginFormDao.deleteByToken(loginToken);
    }

    @Override
    public String createCaptcha(String loginToken) throws InvalidTokenException {
        if (!loginFormDao.tokenExists(loginToken)) {
            throw new InvalidTokenException();
        }

        String captcha = createCode(VALIDATE_CAPTCHA_CODE, CAPTCHA_LENGTH);
        loginFormDao.updateCaptcha(loginToken, captcha);
        return captcha;
    }

    @Override
    public String getCaptcha(String loginToken) throws InvalidTokenException, InvalidCaptchaException {
        LoginForm form = loginFormDao.getByToken(loginToken);
        if (form == null || !form.validateCheckCode()) {
            throw new InvalidTokenException();
        }

        if (form.isNullCaptcha()) {
            throw new InvalidCaptchaException();
        }

        return form.getCaptcha();
    }

    @Override
    public boolean validateCaptcha(String loginToken, String captcha)
            throws InvalidTokenException, InvalidCaptchaException {
        if (StringUtils.isNotBlank(properties.getWhiteCaptcha())
                && StringUtils.equals(properties.getWhiteCaptcha(), captcha)) {
            return true;
        }

        return StringUtils.equals(captcha, getCaptcha(loginToken));
    }

    @Override
    public String createSmsValidateCode(String loginToken, String mobile) throws InvalidTokenException {
        if (!loginFormDao.tokenExists(loginToken)) {
            throw new InvalidTokenException();
        }

        String validateCode = createCode(VALIDATE_SMS_VALIDATE_CODE, SMS_VALIDATE_CODE_LENGTH);

        loginFormDao.updateSmsValidateCode(loginToken, mobile, validateCode);
        loginFormDao.updateCaptcha(loginToken, null);

        return validateCode;
    }

    @Override
    public String getSmsValidateCode(String loginToken) throws InvalidTokenException, InvalidCaptchaException {
        LoginForm form = loginFormDao.getByToken(loginToken);
        if (form == null) {
            throw new InvalidTokenException();
        }

        if (StringUtils.isEmpty(form.getMobileValidateCode())) {
            throw new InvalidCaptchaException();
        }

        return form.getMobileValidateCode();
    }

    @Override
    public boolean validateSmsValidateCode(String loginToken, String mobile, String validateCode)
            throws InvalidTokenException, InvalidCaptchaException {
        if (StringUtils.isNotBlank(properties.getWhiteCaptcha())
                && StringUtils.equals(properties.getWhiteCaptcha(), validateCode)) {
            return true;
        }

        LoginForm form = loginFormDao.getByToken(loginToken);
        if (form == null) {
            throw new InvalidTokenException();

        } else if (StringUtils.isEmpty(form.getMobile()) || StringUtils.isEmpty(form.getMobileValidateCode())) {
            throw new InvalidCaptchaException();
        }

        return StringUtils.equals(form.getMobile(), mobile)
                && StringUtils.equals(form.getMobileValidateCode(), validateCode);
    }

    @Override
    public String getPrivateKey(String loginToken) throws InvalidTokenException {
        LoginForm form = loginFormDao.getByToken(loginToken);
        if (form == null || !form.validateCheckCode()) {
            throw new InvalidTokenException();
        }

        return form.getPrivateKey();
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
