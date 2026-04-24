package com.github.thundax.modules.member.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.thundax.common.Constants;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.member.service.SessionCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wdit
 */
@Service
public class SessionCacheServiceImpl implements SessionCacheService {

    private static final String CACHE_KEY_FORMAT = Constants.CACHE_PREFIX + "admin.session.%s.%s";

    private final RedisClient redisClient;

    private final PooledThreadLocal<Map<String, Object>> localCacheHandler = new PooledThreadLocal<>();

    @Autowired
    public SessionCacheServiceImpl(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String sessionId, String key, Class<T> clazz) {
        Object value = localCacheHandler.computeIfAbsent(HashMap::new)
                .computeIfAbsent(createCacheKey(sessionId, key), cacheKey -> redisClient.get(createCacheKey(sessionId, key), clazz));

        return (T) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String sessionId, String key, TypeReference<T> type) {
        Object value = localCacheHandler.computeIfAbsent(HashMap::new)
                .computeIfAbsent(createCacheKey(sessionId, key), cacheKey -> redisClient.get(createCacheKey(sessionId, key), type));

        return (T) value;
    }

    @Override
    public void put(String sessionId, String key, Object value, int expireSeconds) {
        String cacheKey = createCacheKey(sessionId, key);

        if (expireSeconds < 0) {
            redisClient.set(cacheKey, value);

        } else {
            redisClient.set(cacheKey, value, expireSeconds);
        }

        localCacheHandler.computeIfAbsent(HashMap::new).put(cacheKey, value);
    }

    @Override
    public void remove(String sessionId, String key) {
        String cacheKey = createCacheKey(sessionId, key);

        redisClient.delete(cacheKey);

        localCacheHandler.computeIfAbsent(HashMap::new).remove(cacheKey);
    }

    private String createCacheKey(String sessionId, String key) {
        return String.format(CACHE_KEY_FORMAT, sessionId, key);
    }
}
