package com.github.thundax.modules.auth.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.cache.CacheDTO;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.AccessTokenDao;
import com.github.thundax.modules.auth.entity.AccessToken;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
@Profile("!test")
@EnableConfigurationProperties(AuthProperties.class)
public class AccessTokenDaoImpl implements AccessTokenDao {

    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "AUTH_ONLINE_";

    private static final String TOKEN_PREFIX = CACHE_SECTION + "TOKEN_";

    private static final String USER_ID_PREFIX = CACHE_SECTION + "UID_";

    private static final String TOKEN_INDEX_KEY = "tokens";

    private static final int SAFETY_SECONDS = 5;

    private final AuthProperties properties;

    @CreateCache(name = CACHE_SECTION, cacheType = CacheType.REMOTE)
    private Cache<String, Object> cache;

    @CreateCache(name = CACHE_SECTION + "keys.", cacheType = CacheType.REMOTE)
    private Cache<String, Set<String>> keyIndexCache;

    public AccessTokenDaoImpl(AuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public int count() {
        return countExistingTokenKeys();
    }

    @Override
    public String getUidByToken(String token) {
        return (String) cache.get(TOKEN_PREFIX + token);
    }

    @Override
    public AccessToken getByUserId(String userId) {
        AccessToken accessToken = toDomain((AccessTokenCacheDTO) cache.get(USER_ID_PREFIX + userId));
        if (accessToken != null) {
            accessToken.setUserId(userId);
        }
        return accessToken;
    }

    @Override
    public void insert(AccessToken accessToken) {
        Assert.notNull(accessToken.getToken(), "token can not be null");
        Assert.notNull(accessToken.getUserId(), "userId can not be null");

        int expiredSeconds = properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2;

        AccessTokenCacheDTO cacheDTO = toCacheDTO(accessToken);
        String tokenKey = TOKEN_PREFIX + accessToken.getToken();
        cache.put(tokenKey, accessToken.getUserId(), expiredSeconds, TimeUnit.SECONDS);
        cache.put(USER_ID_PREFIX + accessToken.getUserId(), cacheDTO, expiredSeconds, TimeUnit.SECONDS);
        rememberTokenKey(tokenKey);
    }

    @Override
    public void active(AccessToken accessToken) {
        int expiredSeconds = properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2;

        String tokenKey = TOKEN_PREFIX + accessToken.getToken();
        String userId = (String) cache.get(tokenKey);
        if (userId != null) {
            cache.put(tokenKey, userId, expiredSeconds, TimeUnit.SECONDS);
        }
        AccessTokenCacheDTO cacheDTO = (AccessTokenCacheDTO) cache.get(USER_ID_PREFIX + accessToken.getUserId());
        if (cacheDTO != null) {
            cache.put(USER_ID_PREFIX + accessToken.getUserId(), cacheDTO, expiredSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public void deleteByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String userId = (String) cache.get(tokenKey);
        cache.remove(tokenKey);
        if (userId != null) {
            cache.remove(USER_ID_PREFIX + userId);
        }
        forgetTokenKey(tokenKey);
    }

    private int countExistingTokenKeys() {
        Set<String> keys = keyIndexCache.get(TOKEN_INDEX_KEY);
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
            keyIndexCache.put(TOKEN_INDEX_KEY, existingKeys);
        }
        return existingKeys.size();
    }

    private void rememberTokenKey(String key) {
        Set<String> keys = keyIndexCache.get(TOKEN_INDEX_KEY);
        if (keys == null) {
            keys = new HashSet<>();
        }
        if (keys.add(key)) {
            keyIndexCache.put(TOKEN_INDEX_KEY, keys);
        }
    }

    private void forgetTokenKey(String key) {
        Set<String> keys = keyIndexCache.get(TOKEN_INDEX_KEY);
        if (keys == null) {
            return;
        }
        if (keys.remove(key)) {
            keyIndexCache.put(TOKEN_INDEX_KEY, keys);
        }
    }

    private static AccessToken toDomain(AccessTokenCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        AccessToken accessToken = new AccessToken();
        accessToken.setToken(cacheDTO.token);
        accessToken.setUserId(cacheDTO.userId);
        accessToken.setCheckCode(cacheDTO.checkCode);
        return accessToken;
    }

    private static AccessTokenCacheDTO toCacheDTO(AccessToken accessToken) {
        AccessTokenCacheDTO cacheDTO = new AccessTokenCacheDTO();
        cacheDTO.token = accessToken.getToken();
        cacheDTO.userId = accessToken.getUserId();
        cacheDTO.checkCode = accessToken.getCheckCode();
        return cacheDTO;
    }

    private static class AccessTokenCacheDTO implements CacheDTO {
        private String token;
        private String userId;
        private String checkCode;
    }
}
