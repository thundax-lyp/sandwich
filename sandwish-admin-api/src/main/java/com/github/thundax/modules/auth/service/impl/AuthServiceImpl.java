package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.autoconfigure.LoginProperties;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.utils.IdGen;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.encrypt.Sm2;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.AccessTokenDao;
import com.github.thundax.modules.auth.dao.LoginFormDao;
import com.github.thundax.modules.auth.dao.LoginLockDao;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.InvalidUsernamePasswordException;
import com.github.thundax.modules.auth.exception.TooManyLoginRequestException;
import com.github.thundax.modules.auth.exception.TooManyOnlineUserException;
import com.github.thundax.modules.auth.service.AuthService;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.auth.service.PermissionService;
import com.github.thundax.modules.auth.utils.AuthUtils;
import com.github.thundax.modules.sys.entity.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * AuthServiceImpl
 *
 * @author thundax
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final int CAPTCHA_LENGTH = 4;
    private static final int SMS_VALIDATE_CODE_LENGTH = 6;

    private static final char[] VALIDATE_CAPTCHA_CODE = {'2', '3', '4', '5', '6', '7', '8', '9'};

    private static final char[] VALIDATE_SMS_VALIDATE_CODE = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    private final AuthProperties properties;
    private final LoginProperties loginProperties;
    private final LoginFormDao loginFormDao;
    private final AccessTokenDao accessTokenDao;
    private final LoginLockDao loginLockDao;
    private final PasswordService passwordService;
    private final PermissionService permissionService;

    public AuthServiceImpl(
            AuthProperties properties,
            LoginProperties loginProperties,
            LoginFormDao loginFormDao,
            AccessTokenDao accessTokenDao,
            LoginLockDao loginLockDao,
            PasswordService passwordService,
            PermissionService permissionService) {
        this.properties = properties;
        this.loginProperties = loginProperties;
        this.loginFormDao = loginFormDao;
        this.accessTokenDao = accessTokenDao;
        this.loginLockDao = loginLockDao;
        this.passwordService = passwordService;
        this.permissionService = permissionService;
    }

    @Override
    public LoginForm createLoginForm()
            throws TooManyLoginRequestException, TooManyOnlineUserException {
        // 检测是否登录请求过多
        if (loginFormDao.getLoginCount() > properties.getMaxLoginCount()) {
            throw new TooManyLoginRequestException();
        }

        // 检测是否在线用户过多
        if (accessTokenDao.getOnlineCount() > properties.getMaxOnlineCount()) {
            throw new TooManyOnlineUserException();
        }

        LoginForm form = new LoginForm();
        form.setLoginToken(IdGen.uuid());
        form.setRefreshTokenList(new ArrayList<>(Collections.singletonList(IdGen.uuid())));
        form.setExpiredSeconds(properties.getLoginExpiredSeconds());
        form.setCheckCode(AuthUtils.currentCheckCode());
        form.setCaptcha(createCode(VALIDATE_CAPTCHA_CODE, CAPTCHA_LENGTH));

        Sm2.StringKeyPair keyPair = Sm2.generateKeyPair();
        if (keyPair != null) {
            form.setPublicKey(keyPair.getPublicKey());
            form.setPrivateKey(keyPair.getPrivateKey());
        }

        loginFormDao.save(form);

        return form;
    }

    @Override
    public LoginForm refreshLoginForm(String refreshToken) throws InvalidTokenException {
        // 检测refreshToken是否有效
        LoginForm form = loginFormDao.getByRefreshToken(refreshToken);
        if (form == null || !form.validateCheckCode()) {
            throw new InvalidTokenException();
        }

        List<String> refreshTokenList = new ArrayList<>(form.getRefreshTokenList());
        refreshTokenList.add(0, IdGen.uuid());

        form.setLoginToken(IdGen.uuid());
        form.setRefreshTokenList(refreshTokenList);
        form.setExpiredSeconds(properties.getLoginExpiredSeconds());
        form.setCheckCode(AuthUtils.currentCheckCode());

        loginFormDao.save(form);

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
    public String getCaptcha(String loginToken)
            throws InvalidTokenException, InvalidCaptchaException {
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
    public String createSmsValidateCode(String loginToken, String mobile)
            throws InvalidTokenException {
        if (!loginFormDao.tokenExists(loginToken)) {
            throw new InvalidTokenException();
        }

        String validateCode = createCode(VALIDATE_SMS_VALIDATE_CODE, SMS_VALIDATE_CODE_LENGTH);

        loginFormDao.updateSmsValidateCode(loginToken, mobile, validateCode);
        loginFormDao.updateCaptcha(loginToken, null);

        return validateCode;
    }

    @Override
    public String getSmsValidateCode(String loginToken)
            throws InvalidTokenException, InvalidCaptchaException {
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

        } else if (StringUtils.isEmpty(form.getMobile())
                || StringUtils.isEmpty(form.getMobileValidateCode())) {
            throw new InvalidCaptchaException();
        }

        return StringUtils.equals(form.getMobile(), mobile)
                && StringUtils.equals(form.getMobileValidateCode(), validateCode);
    }

    @Override
    @NonNull
    public AccessToken createAccessToken(String userId) {
        String token = UUID.randomUUID().toString();

        AccessToken accessToken = new AccessToken();
        accessToken.setToken(token);
        accessToken.setUserId(userId);
        accessToken.setCheckCode(AuthUtils.currentCheckCode());

        accessTokenDao.save(accessToken);
        permissionService.createSession(token, userId);

        return accessToken;
    }

    @Override
    public AccessToken getAccessToken(String token) {
        String userId = accessTokenDao.getUidByToken(token);
        if (StringUtils.isEmpty(userId)) {
            return null;
        }

        return accessTokenDao.getByUserId(userId);
    }

    @Override
    public AccessToken findByUserId(String userId) {
        return accessTokenDao.getByUserId(userId);
    }

    @Override
    public boolean validateToken(AccessToken accessToken) {
        return AuthUtils.validateCheckCode(accessToken.getCheckCode());
    }

    @Override
    public void activeAccessToken(AccessToken accessToken) {
        accessTokenDao.active(accessToken);
        permissionService.touch(accessToken.getToken());
    }

    @Override
    public void deleteAccessToken(AccessToken accessToken) {
        accessTokenDao.delete(accessToken);
        permissionService.release(accessToken.getToken());
    }

    @Override
    public void validatePassword(User user, String plainPassword) throws ApiException {
        if (user == null) {
            throw new InvalidUsernamePasswordException();
        }

        if (loginProperties.getEnable() && loginLockDao.isLocked(user.getLoginName())) {
            Long expire = loginLockDao.getLockedExpire(user.getLoginName());
            throw new ApiException("帐号已被锁定，请等待（" + expire + "）秒后自动解锁!");
        }

        if (!passwordService.validate(plainPassword, user.getLoginPass())) {
            if (loginProperties.getEnable()) {
                checkUserLock(user);
            } else {
                throw new InvalidUsernamePasswordException();
            }
        } else {
            loginLockDao.unlock(user.getLoginName());
            loginLockDao.deleteFailCount(user.getLoginName());
        }
    }

    @Override
    public String getPrivateKey(String loginToken) throws InvalidTokenException {
        LoginForm form = loginFormDao.getByToken(loginToken);
        if (form == null || !form.validateCheckCode()) {
            throw new InvalidTokenException();
        }

        return form.getPrivateKey();
    }

    /**
     * 创建验证码
     *
     * @param validateChars 许可的字符
     * @param length 长度
     */
    private String createCode(char[] validateChars, int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < length; idx++) {
            sb.append(validateChars[random.nextInt(validateChars.length)]);
        }
        return sb.toString();
    }

    private void checkUserLock(User user) throws ApiException {
        Integer failCount = loginLockDao.getFailCount(user.getLoginName());
        if (failCount == null) {
            failCount = 1;
            loginLockDao.setFailCount(user.getLoginName(), failCount, loginProperties.getExpire());
        } else {
            failCount = failCount + 1;
            loginLockDao.incrementFailCount(user.getLoginName(), 1L);
        }

        if (failCount + 1 > loginProperties.getMaxFailCount()) {
            loginLockDao.lock(user.getLoginName(), loginProperties.getLockTime());
            loginLockDao.deleteFailCount(user.getLoginName());
            throw new ApiException("帐号已被锁定，请等待（" + loginProperties.getLockTime() + "）秒后自动解锁!");
        } else {
            String message =
                    "密码输入错误"
                            + loginProperties.getMaxFailCount()
                            + "次后将被锁定，剩余"
                            + (loginProperties.getMaxFailCount() - failCount)
                            + "次";
            throw new ApiException(message);
        }
    }
}
