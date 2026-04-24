package com.github.thundax.modules.auth.dao.impl;

import com.github.thundax.common.Constants;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.collect.SetUtils;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.LoginFormDao;
import com.github.thundax.modules.auth.entity.LoginForm;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Set;

/**
 * @author thundax
 */
@Repository
@EnableConfigurationProperties(AuthProperties.class)
public class LoginFormDaoImpl implements LoginFormDao {

    private static final String CACHE_ = Constants.CACHE_PREFIX + "AUTH_LOGIN_";

    /**
     * CACHE_TOKEN_ + token : LoginForm
     */
    private static final String CACHE_TOKEN_ = CACHE_ + "TOKEN_";
    /**
     * CACHE_REFRESH_TOKEN_ + refreshToken : loginToken
     */
    private static final String CACHE_REFRESH_TOKEN_ = CACHE_ + "REFRESH_";

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
        return redisClient.keys(CACHE_REFRESH_TOKEN_).size();
    }


    @Override
    public LoginForm getByToken(String loginToken) {
        LoginForm form = redisClient.get(CACHE_TOKEN_ + loginToken, LoginForm.class);
        if (form != null) {
            form.setLoginToken(loginToken);
        }
        return form;
    }


    @Override
    public LoginForm getByRefreshToken(String refreshToken) {
        String token = redisClient.get(CACHE_REFRESH_TOKEN_ + refreshToken);
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        return getByToken(token);
    }

    @Override
    public void save(LoginForm form) {
        Assert.isTrue(ListUtils.isNotEmpty(form.getRefreshTokenList()), "refreshTokenList can not be null");

        Set<String> removeTokens = SetUtils.newHashSet();
        for (String refreshToken : form.getRefreshTokenList()) {
            String oldToken = redisClient.get(CACHE_REFRESH_TOKEN_ + refreshToken);
            if (!StringUtils.equals(oldToken, form.getLoginToken())) {
                removeTokens.add(oldToken);
            }
        }

        for (int idx = form.getRefreshTokenList().size() - 1; idx >= 0; idx--) {
            String refreshToken = form.getRefreshTokenList().get(idx);
            if (idx == 0) {
                redisClient.set(CACHE_REFRESH_TOKEN_ + refreshToken, form.getLoginToken(),
                        properties.getLoginExpiredSeconds() + SAFETY_SECONDS);

            } else if (idx < LoginForm.REFRESH_TOKEN_SIZE) {
                if (!redisClient.exists(CACHE_REFRESH_TOKEN_ + refreshToken)) {
                    form.getRefreshTokenList().remove(idx);
                } else if (idx == 1) {
                    redisClient.set(CACHE_REFRESH_TOKEN_ + refreshToken, form.getLoginToken(), REFRESH_REMAIN_SECONDS);
                } else {
                    redisClient.set(CACHE_REFRESH_TOKEN_ + refreshToken, form.getLoginToken());
                }

            } else {
                redisClient.delete(CACHE_REFRESH_TOKEN_ + refreshToken);
                form.getRefreshTokenList().remove(idx);
            }
        }

        redisClient.set(CACHE_TOKEN_ + form.getLoginToken(), form,
                properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2);

        SetUtils.forEach(removeTokens,
                token -> redisClient.delete(CACHE_TOKEN_ + token));
    }


    @Override
    public void deleteByToken(String loginToken) {
        LoginForm form = getByToken(loginToken);
        if (form != null) {
            redisClient.delete(CACHE_TOKEN_ + loginToken);
            ListUtils.forEach(form.getRefreshTokenList(),
                    refreshToken -> redisClient.delete(CACHE_REFRESH_TOKEN_ + refreshToken));
        }
    }


    @Override
    public boolean tokenExists(String token) {
        return redisClient.exists(CACHE_TOKEN_ + token);
    }


    @Override
    public void updateCaptcha(String loginToken, String captcha) {
        LoginForm form = getByToken(loginToken);
        if (form != null) {
            form.setCaptcha(captcha);
            redisClient.set(CACHE_TOKEN_ + loginToken, form,
                    properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2);
        }
    }


    @Override
    public void updateSmsValidateCode(String loginToken, String mobile, String validateCode) {
        LoginForm form = getByToken(loginToken);
        if (form != null) {
            form.setMobile(mobile);
            form.setMobileValidateCode(validateCode);

            redisClient.set(CACHE_TOKEN_ + loginToken, form,
                    properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2);
        }
    }

}
