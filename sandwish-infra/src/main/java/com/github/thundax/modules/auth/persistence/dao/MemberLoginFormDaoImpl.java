package com.github.thundax.modules.auth.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.cache.CacheDTO;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.MemberLoginFormDao;
import com.github.thundax.modules.auth.entity.MemberLoginForm;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
@Profile("!test")
@EnableConfigurationProperties(AuthProperties.class)
public class MemberLoginFormDaoImpl implements MemberLoginFormDao {

    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "MEMBER_LOGIN_";
    private static final String TOKEN_PREFIX = CACHE_SECTION + "TOKEN_";
    private static final String REFRESH_TOKEN_PREFIX = CACHE_SECTION + "REFRESH_";
    private static final String REFRESH_INDEX_KEY = "memberRefreshTokens";
    private static final int REFRESH_REMAIN_SECONDS = 60;
    private static final int SAFETY_SECONDS = 5;

    private final AuthProperties properties;

    @CreateCache(name = CACHE_SECTION, cacheType = CacheType.REMOTE)
    private Cache<String, Object> cache;

    @CreateCache(name = CACHE_SECTION + "keys.", cacheType = CacheType.REMOTE)
    private Cache<String, Set<String>> keyIndexCache;

    public MemberLoginFormDaoImpl(AuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public int count() {
        return countExistingRefreshKeys();
    }

    @Override
    public MemberLoginForm getByToken(String loginToken) {
        MemberLoginForm form = toDomain((MemberLoginFormCacheDTO) cache.get(TOKEN_PREFIX + loginToken));
        if (form != null) {
            form.setLoginToken(loginToken);
        }
        return form;
    }

    @Override
    public MemberLoginForm getByRefreshToken(String refreshToken) {
        String token = (String) cache.get(REFRESH_TOKEN_PREFIX + refreshToken);
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        return getByToken(token);
    }

    @Override
    public void insert(MemberLoginForm form) {
        Assert.isTrue(
                form.getRefreshTokenList() != null
                        && !form.getRefreshTokenList().isEmpty(),
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

            } else if (idx < MemberLoginForm.REFRESH_TOKEN_SIZE) {
                if (cache.get(refreshKey) == null) {
                    form.getRefreshTokenList().remove(idx);
                } else if (idx == 1) {
                    cache.put(refreshKey, form.getLoginToken(), REFRESH_REMAIN_SECONDS, TimeUnit.SECONDS);
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

        putForm(form);
        removeTokens.forEach(token -> cache.remove(TOKEN_PREFIX + token));
    }

    @Override
    public void deleteByToken(String loginToken) {
        MemberLoginForm form = getByToken(loginToken);
        if (form != null) {
            cache.remove(TOKEN_PREFIX + loginToken);
            if (form.getRefreshTokenList() != null
                    && !form.getRefreshTokenList().isEmpty()) {
                form.getRefreshTokenList().forEach(refreshToken -> {
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
        MemberLoginForm form = getByToken(loginToken);
        if (form != null) {
            form.setCaptcha(captcha);
            putForm(form);
        }
    }

    @Override
    public void updateSmsValidateCode(String loginToken, String mobile, String validateCode) {
        MemberLoginForm form = getByToken(loginToken);
        if (form != null) {
            form.setMobile(mobile);
            form.setMobileValidateCode(validateCode);
            putForm(form);
        }
    }

    @Override
    public void updateEmailValidateCode(String loginToken, String email, String validateCode) {
        MemberLoginForm form = getByToken(loginToken);
        if (form != null) {
            form.setEmail(email);
            form.setEmailValidateCode(validateCode);
            putForm(form);
        }
    }

    @Override
    public void updateKeyPair(String loginToken, String publicKey, String privateKey) {
        MemberLoginForm form = getByToken(loginToken);
        if (form != null) {
            form.setPublicKey(publicKey);
            form.setPrivateKey(privateKey);
            putForm(form);
        }
    }

    private void putForm(MemberLoginForm form) {
        cache.put(
                TOKEN_PREFIX + form.getLoginToken(),
                toCacheDTO(form),
                properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2,
                TimeUnit.SECONDS);
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

    private static MemberLoginForm toDomain(MemberLoginFormCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        MemberLoginForm form = new MemberLoginForm();
        form.setLoginToken(cacheDTO.loginToken);
        form.setRefreshTokenList(cacheDTO.refreshTokenList);
        form.setCaptcha(cacheDTO.captcha);
        form.setMobile(cacheDTO.mobile);
        form.setMobileValidateCode(cacheDTO.mobileValidateCode);
        form.setEmail(cacheDTO.email);
        form.setEmailValidateCode(cacheDTO.emailValidateCode);
        form.setExpiredSeconds(cacheDTO.expiredSeconds);
        form.setCheckCode(cacheDTO.checkCode);
        form.setPublicKey(cacheDTO.publicKey);
        form.setPrivateKey(cacheDTO.privateKey);
        return form;
    }

    private static MemberLoginFormCacheDTO toCacheDTO(MemberLoginForm form) {
        MemberLoginFormCacheDTO cacheDTO = new MemberLoginFormCacheDTO();
        cacheDTO.loginToken = form.getLoginToken();
        cacheDTO.refreshTokenList = form.getRefreshTokenList();
        cacheDTO.captcha = form.getCaptcha();
        cacheDTO.mobile = form.getMobile();
        cacheDTO.mobileValidateCode = form.getMobileValidateCode();
        cacheDTO.email = form.getEmail();
        cacheDTO.emailValidateCode = form.getEmailValidateCode();
        cacheDTO.expiredSeconds = form.getExpiredSeconds();
        cacheDTO.checkCode = form.getCheckCode();
        cacheDTO.publicKey = form.getPublicKey();
        cacheDTO.privateKey = form.getPrivateKey();
        return cacheDTO;
    }

    private static class MemberLoginFormCacheDTO implements CacheDTO {
        private String loginToken;
        private List<String> refreshTokenList;
        private String captcha;
        private String mobile;
        private String mobileValidateCode;
        private String email;
        private String emailValidateCode;
        private Integer expiredSeconds;
        private String checkCode;
        private String publicKey;
        private String privateKey;
    }
}
