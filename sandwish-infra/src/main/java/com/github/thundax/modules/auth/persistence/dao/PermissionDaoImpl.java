package com.github.thundax.modules.auth.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.modules.auth.dao.PermissionDao;
import com.github.thundax.modules.auth.entity.PermissionSession;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
@Profile("!test")
public class PermissionDaoImpl implements PermissionDao {

    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "AUTH_PERMISSION_";

    private static final String TOKEN_PREFIX = CACHE_SECTION + "TOKEN_";
    private static final String TOKEN_INDEX_KEY = "tokens";

    @CreateCache(name = CACHE_SECTION, cacheType = CacheType.REMOTE)
    private Cache<String, PermissionSession> cache;

    @CreateCache(name = CACHE_SECTION + "keys.", cacheType = CacheType.REMOTE)
    private Cache<String, Set<String>> keyIndexCache;

    @Override
    public PermissionSession getByToken(String token) {
        return cache.get(TOKEN_PREFIX + token);
    }

    @Override
    public void insert(PermissionSession session, int expiredSeconds) {
        Assert.hasText(session.getToken(), "token can not be empty");
        String key = TOKEN_PREFIX + session.getToken();
        cache.put(key, session, expiredSeconds, TimeUnit.SECONDS);
        rememberTokenKey(key);
    }

    @Override
    public void touch(String token, int expiredSeconds) {
        String key = TOKEN_PREFIX + token;
        PermissionSession session = cache.get(key);
        if (session != null) {
            cache.put(key, session, expiredSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public void deleteByToken(String token) {
        String key = TOKEN_PREFIX + token;
        cache.remove(key);
        forgetTokenKey(key);
    }

    @Override
    public void deleteAll() {
        Set<String> keys = keyIndexCache.get(TOKEN_INDEX_KEY);
        if (keys != null && !keys.isEmpty()) {
            cache.removeAll(keys);
        }
        keyIndexCache.remove(TOKEN_INDEX_KEY);
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
}
