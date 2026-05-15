package com.github.thundax.modules.auth.security;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class OpenApiNonceStore {

    public static final long DEFAULT_TTL_MILLIS = 5 * 60 * 1000L;

    private final long ttlMillis;
    private final Map<String, Long> nonceExpiresAt = new ConcurrentHashMap<>();

    public OpenApiNonceStore() {
        this(DEFAULT_TTL_MILLIS);
    }

    OpenApiNonceStore(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    public boolean markUsed(String apiKey, String nonce) {
        return markUsed(apiKey, nonce, System.currentTimeMillis());
    }

    boolean markUsed(String apiKey, String nonce, long now) {
        if (StringUtils.isBlank(apiKey) || StringUtils.isBlank(nonce)) {
            return false;
        }
        purgeExpired(now);
        Long previous = nonceExpiresAt.putIfAbsent(key(apiKey, nonce), now + ttlMillis);
        return previous == null || previous <= now;
    }

    private void purgeExpired(long now) {
        Iterator<Map.Entry<String, Long>> iterator = nonceExpiresAt.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() <= now) {
                iterator.remove();
            }
        }
    }

    private String key(String apiKey, String nonce) {
        return "open-api:nonce:" + apiKey + ":" + nonce;
    }
}
