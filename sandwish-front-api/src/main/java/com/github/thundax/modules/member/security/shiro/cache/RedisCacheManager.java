package com.github.thundax.modules.member.security.shiro.cache;

import com.github.thundax.common.Constants;
import com.github.thundax.common.thread.PooledThreadLocal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

/**
 * 自定义授权缓存管理类
 *
 * @author wdit
 */
public class RedisCacheManager implements CacheManager {

    private static final String CACHE_PREFIX = Constants.CACHE_PREFIX + "interaction-shiro.";

    private long expireSeconds = 1200;
    private ShiroJetCacheStore cacheStore;

    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        return new RedisSessionCache<>(CACHE_PREFIX + name, cacheStore, expireSeconds);
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public void setCacheStore(ShiroJetCacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    /** SESSION缓存管理类 */
    public static class RedisSessionCache<K, V> implements Cache<K, V> {

        private final ShiroJetCacheStore cacheStore;
        private final long expireSeconds;
        private final String cacheKeyPrefix;

        private final PooledThreadLocal<Map<String, V>> localCacheHandler =
                new PooledThreadLocal<>();

        public RedisSessionCache(
                String cacheKeyPrefix,
                ShiroJetCacheStore cacheStore,
                long expireSeconds) {
            this.cacheKeyPrefix = cacheKeyPrefix;
            this.cacheStore = cacheStore;
            this.expireSeconds = expireSeconds;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V get(K key) throws CacheException {
            if (key == null) {
                return null;
            }

            Map<String, V> localCache = localCacheHandler.computeIfAbsent(HashMap::new);

            return localCache.computeIfAbsent(
                    createCacheKey(key),
                    cacheKey -> (V) cacheStore.get(cacheKey));
        }

        @Override
        public V put(K key, V value) throws CacheException {
            if (key == null) {
                return null;
            }

            String cacheKey = createCacheKey(key);

            Map<String, V> localCache = localCacheHandler.computeIfAbsent(HashMap::new);

            if (value == null) {
                cacheStore.remove(cacheKey);
                forgetKey(cacheKey);
                localCache.remove(cacheKey);

            } else {
                cacheStore.put(cacheKey, value, expireSeconds);
                rememberKey(cacheKey);
                localCache.put(cacheKey, value);
            }

            return value;
        }

        @Override
        public V remove(K key) throws CacheException {
            if (key == null) {
                return null;
            }

            V value = get(key);

            String cacheKey = createCacheKey(key);
            cacheStore.remove(cacheKey);
            forgetKey(cacheKey);

            Map<String, V> localCache = localCacheHandler.computeIfAbsent(HashMap::new);
            localCache.remove(cacheKey);

            return value;
        }

        @Override
        public void clear() throws CacheException {
            Set<String> keys = cacheStore.getIndex(createIndexKey());
            if (keys != null) {
                cacheStore.removeAll(keys);
                cacheStore.removeIndex(createIndexKey());
            }

            localCacheHandler.remove();
        }

        @Override
        public int size() {
            return activeKeys().size();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Set<K> keys() {
            Set<K> result = new HashSet<>();
            for (String key : activeKeys()) {
                result.add((K) key.substring(cacheKeyPrefix.length()));
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Collection<V> values() {
            Set<V> values = new HashSet<>();
            for (String key : activeKeys()) {
                values.add((V) cacheStore.get(key));
            }

            return values;
        }

        private String createCacheKey(K key) {
            if (key == null) {
                return null;
            }
            return this.cacheKeyPrefix + key;
        }

        private String createIndexKey() {
            return this.cacheKeyPrefix;
        }

        private void rememberKey(String cacheKey) {
            Set<String> keys = cacheStore.getIndex(createIndexKey());
            if (keys == null) {
                keys = new HashSet<>();
            }
            keys.add(cacheKey);
            cacheStore.putIndex(createIndexKey(), keys, expireSeconds);
        }

        private void forgetKey(String cacheKey) {
            Set<String> keys = cacheStore.getIndex(createIndexKey());
            if (keys == null) {
                return;
            }

            keys.remove(cacheKey);
            if (keys.isEmpty()) {
                cacheStore.removeIndex(createIndexKey());
            } else {
                cacheStore.putIndex(createIndexKey(), keys, expireSeconds);
            }
        }

        private Set<String> activeKeys() {
            Set<String> keys = cacheStore.getIndex(createIndexKey());
            if (keys == null || keys.isEmpty()) {
                return new HashSet<>();
            }

            Set<String> activeKeys = new HashSet<>();
            for (String key : keys) {
                if (cacheStore.get(key) != null) {
                    activeKeys.add(key);
                }
            }

            if (activeKeys.size() != keys.size()) {
                if (activeKeys.isEmpty()) {
                    cacheStore.removeIndex(createIndexKey());
                } else {
                    cacheStore.putIndex(createIndexKey(), activeKeys, expireSeconds);
                }
            }
            return activeKeys;
        }
    }
}
