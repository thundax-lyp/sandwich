package com.github.thundax.modules.auth.persistence.dao;

import com.github.thundax.common.Constants;
import org.apache.commons.lang3.StringUtils;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.LoginFormDao;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.persistence.assembler.AuthPersistenceAssembler;
import com.github.thundax.modules.auth.persistence.dataobject.LoginFormDO;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/** login form Redis DAO 实现。 */
@Repository
@Profile("!test")
@EnableConfigurationProperties(AuthProperties.class)
public class LoginFormDaoImpl implements LoginFormDao {

    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "AUTH_LOGIN_";

    /** TOKEN_PREFIX + token : LoginForm */
    private static final String TOKEN_PREFIX = CACHE_SECTION + "TOKEN_";
    /** REFRESH_TOKEN_PREFIX + refreshToken : loginToken */
    private static final String REFRESH_TOKEN_PREFIX = CACHE_SECTION + "REFRESH_";

    private static final int REFRESH_REMAIN_SECONDS = 60;

    private static final int SAFETY_SECONDS = 5;

    private final AuthProperties properties;
    private final RedisClient redisClient;

    public LoginFormDaoImpl(RedisClient redisClient, AuthProperties properties) {
        this.redisClient = redisClient;
        this.properties = properties;
    }

    @Override
    public int getLoginCount() {
        return redisClient.keys(REFRESH_TOKEN_PREFIX).size();
    }

    @Override
    public LoginForm getByToken(String loginToken) {
        LoginFormDO formDO = redisClient.get(TOKEN_PREFIX + loginToken, LoginFormDO.class);
        LoginForm form = AuthPersistenceAssembler.toEntity(formDO);
        if (form != null) {
            form.setLoginToken(loginToken);
        }
        return form;
    }

    @Override
    public LoginForm getByRefreshToken(String refreshToken) {
        String token = redisClient.get(REFRESH_TOKEN_PREFIX + refreshToken);
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        return getByToken(token);
    }

    @Override
    public void save(LoginForm form) {
        Assert.isTrue(
                form.getRefreshTokenList() != null && !form.getRefreshTokenList().isEmpty(),
                "refreshTokenList can not be null");

        Set<String> removeTokens = new HashSet<>();
        for (String refreshToken : form.getRefreshTokenList()) {
            String oldToken = redisClient.get(REFRESH_TOKEN_PREFIX + refreshToken);
            if (!StringUtils.equals(oldToken, form.getLoginToken())) {
                removeTokens.add(oldToken);
            }
        }

        for (int idx = form.getRefreshTokenList().size() - 1; idx >= 0; idx--) {
            String refreshToken = form.getRefreshTokenList().get(idx);
            if (idx == 0) {
                redisClient.set(
                        REFRESH_TOKEN_PREFIX + refreshToken,
                        form.getLoginToken(),
                        properties.getLoginExpiredSeconds() + SAFETY_SECONDS);

            } else if (idx < LoginForm.REFRESH_TOKEN_SIZE) {
                if (!redisClient.exists(REFRESH_TOKEN_PREFIX + refreshToken)) {
                    form.getRefreshTokenList().remove(idx);
                } else if (idx == 1) {
                    redisClient.set(
                            REFRESH_TOKEN_PREFIX + refreshToken,
                            form.getLoginToken(),
                            REFRESH_REMAIN_SECONDS);
                } else {
                    redisClient.set(REFRESH_TOKEN_PREFIX + refreshToken, form.getLoginToken());
                }

            } else {
                redisClient.delete(REFRESH_TOKEN_PREFIX + refreshToken);
                form.getRefreshTokenList().remove(idx);
            }
        }

        LoginFormDO formDO = AuthPersistenceAssembler.toDataObject(form);
        redisClient.set(
                TOKEN_PREFIX + form.getLoginToken(),
                formDO,
                properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2);

        removeTokens.forEach(token -> redisClient.delete(TOKEN_PREFIX + token));
    }

    @Override
    public void deleteByToken(String loginToken) {
        LoginForm form = getByToken(loginToken);
        if (form != null) {
            redisClient.delete(TOKEN_PREFIX + loginToken);
            if (form.getRefreshTokenList() != null && !form.getRefreshTokenList().isEmpty()) {
                form.getRefreshTokenList()
                        .forEach(
                                refreshToken ->
                                        redisClient.delete(REFRESH_TOKEN_PREFIX + refreshToken));
            }
        }
    }

    @Override
    public boolean tokenExists(String token) {
        return redisClient.exists(TOKEN_PREFIX + token);
    }

    @Override
    public void updateCaptcha(String loginToken, String captcha) {
        LoginForm form = getByToken(loginToken);
        if (form != null) {
            form.setCaptcha(captcha);
            LoginFormDO formDO = AuthPersistenceAssembler.toDataObject(form);
            redisClient.set(
                    TOKEN_PREFIX + loginToken,
                    formDO,
                    properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2);
        }
    }

    @Override
    public void updateSmsValidateCode(String loginToken, String mobile, String validateCode) {
        LoginForm form = getByToken(loginToken);
        if (form != null) {
            form.setMobile(mobile);
            form.setMobileValidateCode(validateCode);
            LoginFormDO formDO = AuthPersistenceAssembler.toDataObject(form);
            redisClient.set(
                    TOKEN_PREFIX + loginToken,
                    formDO,
                    properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2);
        }
    }
}
