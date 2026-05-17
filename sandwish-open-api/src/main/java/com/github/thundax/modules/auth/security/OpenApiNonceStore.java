package com.github.thundax.modules.auth.security;

import io.lettuce.core.RedisClient;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenApiNonceStore {

    public static final long DEFAULT_TTL_MILLIS = 5 * 60 * 1000L;

    private static final String NONCE_KEY_PREFIX = "open-api:nonce:";
    private static final String USED_VALUE = "1";
    private static final String OK = "OK";

    private final long ttlMillis;
    private final RedisClient redisClient;
    private final NonceWriter nonceWriter;
    private StatefulRedisConnection<String, String> redisConnection;

    public OpenApiNonceStore(@Value("${spring.redis.url:redis://127.0.0.1:6379/0}") String redisUrl) {
        this(DEFAULT_TTL_MILLIS, RedisClient.create(redisUrl), null);
    }

    OpenApiNonceStore(long ttlMillis, NonceWriter nonceWriter) {
        this(ttlMillis, null, nonceWriter);
    }

    private OpenApiNonceStore(long ttlMillis, RedisClient redisClient, NonceWriter nonceWriter) {
        this.ttlMillis = ttlMillis;
        this.redisClient = redisClient;
        this.nonceWriter = nonceWriter;
    }

    public boolean markUsed(String apiKey, String nonce) {
        if (StringUtils.isBlank(apiKey) || StringUtils.isBlank(nonce)) {
            return false;
        }
        return setIfAbsent(key(apiKey, nonce));
    }

    @PreDestroy
    public void destroy() {
        if (redisConnection != null) {
            redisConnection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
    }

    private boolean setIfAbsent(String key) {
        if (nonceWriter != null) {
            return nonceWriter.setIfAbsent(key, USED_VALUE, ttlMillis);
        }
        return OK.equals(redis().set(key, USED_VALUE, SetArgs.Builder.nx().px(ttlMillis)));
    }

    private RedisCommands<String, String> redis() {
        if (redisConnection == null) {
            synchronized (this) {
                if (redisConnection == null) {
                    redisConnection = redisClient.connect();
                }
            }
        }
        return redisConnection.sync();
    }

    private String key(String apiKey, String nonce) {
        return NONCE_KEY_PREFIX + apiKey + ":" + nonce;
    }

    interface NonceWriter {
        boolean setIfAbsent(String key, String value, long ttlMillis);
    }
}
