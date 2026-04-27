package com.github.thundax.modules.member.security.shiro.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/** Shiro 迁移期 JetCache 存储适配。 */
@Component
public class ShiroJetCacheStore {

    private static final String CACHE_PREFIX = Constants.CACHE_PREFIX + "interaction-shiro.";
    private static final String KEY_INDEX_CACHE_PREFIX =
            Constants.CACHE_PREFIX + "interaction-shiro-keys.";

    @CreateCache(name = CACHE_PREFIX, cacheType = CacheType.REMOTE)
    private Cache<String, Object> cache;

    @CreateCache(name = KEY_INDEX_CACHE_PREFIX, cacheType = CacheType.REMOTE)
    private Cache<String, Set<String>> keyIndexCache;

    public Object get(String key) {
        return cache.get(key);
    }

    public void put(String key, Object value, long expireSeconds) {
        cache.put(key, value, expireSeconds, TimeUnit.SECONDS);
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public void removeAll(Set<String> keys) {
        cache.removeAll(keys);
    }

    public Set<String> getIndex(String indexKey) {
        Set<String> keys = keyIndexCache.get(indexKey);
        return keys == null ? null : new HashSet<>(keys);
    }

    public void putIndex(String indexKey, Set<String> keys, long expireSeconds) {
        keyIndexCache.put(indexKey, new HashSet<>(keys), expireSeconds, TimeUnit.SECONDS);
    }

    public void removeIndex(String indexKey) {
        keyIndexCache.remove(indexKey);
    }
}
