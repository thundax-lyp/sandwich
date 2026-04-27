package com.github.thundax.modules.member.security.shiro.cache;

import com.github.thundax.common.Constants;
import com.github.thundax.common.thread.PooledThreadLocal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 自定义授权缓存管理类
 *
 * @author wdit
 */
public class RedisCacheManager implements CacheManager {

    private static final String CACHE_PREFIX = Constants.CACHE_PREFIX + "interaction-shiro.";

    private RedisTemplate<String, Object> redisTemplate;
    private long expireSeconds = 1200;

    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        return new RedisSessionCache<>(CACHE_PREFIX + name, redisTemplate, expireSeconds);
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    /** SESSION缓存管理类 */
    public static class RedisSessionCache<K, V> implements Cache<K, V> {

        private final RedisTemplate<String, Object> redisTemplate;
        private final long expireSeconds;
        private final String cacheKeyPrefix;

        private final PooledThreadLocal<Map<String, V>> localCacheHandler =
                new PooledThreadLocal<>();

        public RedisSessionCache(
                String cacheKeyPrefix,
                RedisTemplate<String, Object> redisTemplate,
                long expireSeconds) {
            this.cacheKeyPrefix = cacheKeyPrefix;
            this.redisTemplate = redisTemplate;
            this.expireSeconds = expireSeconds;

            RedisSerializer<String> keySerializer = new StringRedisSerializer();
            this.redisTemplate.setKeySerializer(keySerializer);
            this.redisTemplate.setHashKeySerializer(keySerializer);

            RedisSerializer<Object> valueSerializer = new JdkSerializationRedisSerializer();
            this.redisTemplate.setValueSerializer(valueSerializer);
            this.redisTemplate.setHashValueSerializer(valueSerializer);
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
                    cacheKey -> {
                        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
                        return (V) valueOps.get(cacheKey);
                    });
        }

        @Override
        public V put(K key, V value) throws CacheException {
            if (key == null) {
                return null;
            }

            String cacheKey = createCacheKey(key);

            Map<String, V> localCache = localCacheHandler.computeIfAbsent(HashMap::new);

            if (value == null) {
                redisTemplate.delete(cacheKey);
                localCache.remove(cacheKey);

            } else {
                ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
                valueOps.set(cacheKey, value);
                redisTemplate.expire(cacheKey, expireSeconds, TimeUnit.SECONDS);

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
            redisTemplate.delete(cacheKey);

            Map<String, V> localCache = localCacheHandler.computeIfAbsent(HashMap::new);
            localCache.remove(cacheKey);

            return value;
        }

        @Override
        public void clear() throws CacheException {
            Set<String> keys = redisTemplate.keys(cacheKeyPrefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

            localCacheHandler.remove();
        }

        @Override
        public int size() {
            Set<String> keys = redisTemplate.keys(cacheKeyPrefix + "*");
            return keys == null ? 0 : keys.size();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Set<K> keys() {
            Set<String> keys = redisTemplate.keys(cacheKeyPrefix + "*");
            if (keys == null || keys.isEmpty()) {
                return new HashSet<>();
            }

            Set<K> result = new HashSet<>();
            for (String key : keys) {
                result.add((K) key.substring(cacheKeyPrefix.length()));
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Collection<V> values() {
            Set<String> keys = redisTemplate.keys(cacheKeyPrefix + "*");
            if (keys == null || keys.isEmpty()) {
                return new HashSet<>();
            }

            ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();

            Set<V> values = new HashSet<>();
            for (String key : keys) {
                values.add((V) valueOps.get(key));
            }

            return values;
        }

        private String createCacheKey(K key) {
            if (key == null) {
                return null;
            }
            return this.cacheKeyPrefix + key;
        }
    }
}
