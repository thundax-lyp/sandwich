package com.github.thundax.modules.auth.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.LoginFormDao;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.persistence.assembler.AuthPersistenceAssembler;
import com.github.thundax.modules.auth.persistence.dataobject.LoginFormDO;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
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

    private static final String REFRESH_INDEX_KEY = "refreshTokens";

    private static final int REFRESH_REMAIN_SECONDS = 60;

    private static final int SAFETY_SECONDS = 5;

    private final AuthProperties properties;

    @CreateCache(name = CACHE_SECTION, cacheType = CacheType.REMOTE)
    private Cache<String, Object> cache;

    @CreateCache(name = CACHE_SECTION + "keys.", cacheType = CacheType.REMOTE)
    private Cache<String, Set<String>> keyIndexCache;

    public LoginFormDaoImpl(AuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public int getLoginCount() {
        return countExistingRefreshKeys();
    }

    @Override
    public LoginForm getByToken(String loginToken) {
        LoginFormDO formDO = (LoginFormDO) cache.get(TOKEN_PREFIX + loginToken);
        LoginForm form = AuthPersistenceAssembler.toEntity(formDO);
        if (form != null) {
            form.setLoginToken(loginToken);
        }
        return form;
    }

    @Override
    public LoginForm getByRefreshToken(String refreshToken) {
        String token = (String) cache.get(REFRESH_TOKEN_PREFIX + refreshToken);
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
            String oldToken = (String) cache.get(REFRESH_TOKEN_PREFIX + refreshToken);
            if (!StringUtils.equals(oldToken, form.getLoginToken())) {
                removeTokens.add(oldToken);
            }
        }

        for (int idx = form.getRefreshTokenList().size() - 1; idx >= 0; idx--) {
            String refreshToken = form.getRefreshTokenList().get(idx);
            String refreshKey = REFRESH_TOKEN_PREFIX + refreshToken;
            if (idx == 0) {
                cache.put(
                        refreshKey,
                        form.getLoginToken(),
                        properties.getLoginExpiredSeconds() + SAFETY_SECONDS,
                        TimeUnit.SECONDS);
                rememberRefreshKey(refreshKey);

            } else if (idx < LoginForm.REFRESH_TOKEN_SIZE) {
                if (cache.get(refreshKey) == null) {
                    form.getRefreshTokenList().remove(idx);
                } else if (idx == 1) {
                    cache.put(
                            refreshKey,
                            form.getLoginToken(),
                            REFRESH_REMAIN_SECONDS,
                            TimeUnit.SECONDS);
                    rememberRefreshKey(refreshKey);
                } else {
                    cache.put(refreshKey, form.getLoginToken());
                    rememberRefreshKey(refreshKey);
                }

            } else {
                cache.remove(refreshKey);
                forgetRefreshKey(refreshKey);
                form.getRefreshTokenList().remove(idx);
            }
        }

        LoginFormDO formDO = AuthPersistenceAssembler.toDataObject(form);
        cache.put(
                TOKEN_PREFIX + form.getLoginToken(),
                formDO,
                properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2,
                TimeUnit.SECONDS);

        removeTokens.forEach(token -> cache.remove(TOKEN_PREFIX + token));
    }

    @Override
    public void deleteByToken(String loginToken) {
        LoginForm form = getByToken(loginToken);
        if (form != null) {
            cache.remove(TOKEN_PREFIX + loginToken);
            if (form.getRefreshTokenList() != null && !form.getRefreshTokenList().isEmpty()) {
                form.getRefreshTokenList()
                        .forEach(
                                refreshToken -> {
                                    String refreshKey = REFRESH_TOKEN_PREFIX + refreshToken;
                                    cache.remove(refreshKey);
                                    forgetRefreshKey(refreshKey);
                                });
            }
        }
    }

    @Override
    public boolean tokenExists(String token) {
        return cache.get(TOKEN_PREFIX + token) != null;
    }

    @Override
    public void updateCaptcha(String loginToken, String captcha) {
        LoginForm form = getByToken(loginToken);
        if (form != null) {
            form.setCaptcha(captcha);
            LoginFormDO formDO = AuthPersistenceAssembler.toDataObject(form);
            cache.put(
                    TOKEN_PREFIX + loginToken,
                    formDO,
                    properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void updateSmsValidateCode(String loginToken, String mobile, String validateCode) {
        LoginForm form = getByToken(loginToken);
        if (form != null) {
            form.setMobile(mobile);
            form.setMobileValidateCode(validateCode);
            LoginFormDO formDO = AuthPersistenceAssembler.toDataObject(form);
            cache.put(
                    TOKEN_PREFIX + loginToken,
                    formDO,
                    properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2,
                    TimeUnit.SECONDS);
        }
    }

    private int countExistingRefreshKeys() {
        Set<String> keys = keyIndexCache.get(REFRESH_INDEX_KEY);
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        Set<String> existingKeys = new HashSet<>();
        for (String key : keys) {
            if (cache.get(key) != null) {
                existingKeys.add(key);
            }
        }
        if (existingKeys.size() != keys.size()) {
            keyIndexCache.put(REFRESH_INDEX_KEY, existingKeys);
        }
        return existingKeys.size();
    }

    private void rememberRefreshKey(String key) {
        Set<String> keys = keyIndexCache.get(REFRESH_INDEX_KEY);
        if (keys == null) {
            keys = new HashSet<>();
        }
        if (keys.add(key)) {
            keyIndexCache.put(REFRESH_INDEX_KEY, keys);
        }
    }

    private void forgetRefreshKey(String key) {
        Set<String> keys = keyIndexCache.get(REFRESH_INDEX_KEY);
        if (keys == null) {
            return;
        }
        if (keys.remove(key)) {
            keyIndexCache.put(REFRESH_INDEX_KEY, keys);
        }
    }
}
