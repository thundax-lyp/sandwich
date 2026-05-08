package com.github.thundax.modules.auth.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.modules.auth.dao.PreAuthSessionDao;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.PreAuthSession.RefreshTokenValue;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
@Profile("!test")
public class PreAuthSessionDaoImpl implements PreAuthSessionDao {

    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "PRE_AUTH_SESSION_";
    private static final String SESSION_PREFIX = CACHE_SECTION + "SESSION_";
    private static final String TOKEN_PREFIX = CACHE_SECTION + "TOKEN_";
    private static final String REFRESH_TOKEN_PREFIX = CACHE_SECTION + "REFRESH_";
    private static final String ACTIVE_SESSION_KEY = CACHE_SECTION + "ACTIVE";
    private static final int SAFETY_SECONDS = 5;

    @CreateCache(name = CACHE_SECTION, cacheType = CacheType.REMOTE)
    private Cache<String, Object> cache;

    private final RedisClient redisClient;
    private StatefulRedisConnection<String, String> redisConnection;

    public PreAuthSessionDaoImpl(@Value("${spring.redis.url:redis://127.0.0.1:6379/0}") String redisUrl) {
        this.redisClient = RedisClient.create(redisUrl);
    }

    @Override
    public int count() {
        removeExpiredActiveSessions();
        return redis().zcard(ACTIVE_SESSION_KEY).intValue();
    }

    @Override
    public PreAuthSession getById(PreAuthSessionId id) {
        return id == null ? null : (PreAuthSession) cache.get(SESSION_PREFIX + id.asString());
    }

    @Override
    public PreAuthSessionId getIdByToken(PreAuthSessionToken token) {
        if (token == null) {
            return null;
        }
        return PreAuthSessionId.ofNullable((String) cache.get(TOKEN_PREFIX + token.asString()));
    }

    @Override
    public PreAuthSessionId getIdByRefreshToken(PreAuthSessionToken refreshToken) {
        if (refreshToken == null) {
            return null;
        }
        return PreAuthSessionId.ofNullable((String) cache.get(REFRESH_TOKEN_PREFIX + refreshToken.asString()));
    }

    @Override
    public void insert(PreAuthSession session) {
        Assert.notNull(session, "preAuthSession can not be null");
        putSession(session);
    }

    @Override
    public void update(PreAuthSession session) {
        Assert.notNull(session, "preAuthSession can not be null");
        PreAuthSession oldSession = getById(session.getId());
        if (oldSession != null) {
            removeIndexes(oldSession);
        }
        putSession(session);
    }

    @Override
    public void deleteById(PreAuthSessionId id) {
        PreAuthSession session = getById(id);
        if (session == null) {
            return;
        }
        cache.remove(SESSION_PREFIX + id.asString());
        removeIndexes(session);
        redis().zrem(ACTIVE_SESSION_KEY, id.asString());
    }

    private void putSession(PreAuthSession session) {
        long seconds = remainingSeconds(session);
        String sessionKey = SESSION_PREFIX + session.getId().asString();
        cache.put(sessionKey, session, seconds + SAFETY_SECONDS, TimeUnit.SECONDS);
        redis().zadd(ACTIVE_SESSION_KEY, session.getExpiredAt(), session.getId().asString());
        cache.put(TOKEN_PREFIX + session.getToken().asString(), session.getId().asString(), seconds, TimeUnit.SECONDS);
        for (RefreshTokenValue refreshToken : session.refreshTokenValues()) {
            long refreshTokenSeconds = ttlSeconds(refreshToken.getExpiredAt());
            if (refreshTokenSeconds <= 0) {
                continue;
            }
            cache.put(
                    REFRESH_TOKEN_PREFIX + refreshToken.getToken().asString(),
                    session.getId().asString(),
                    refreshTokenSeconds,
                    TimeUnit.SECONDS);
        }
    }

    private void removeIndexes(PreAuthSession session) {
        cache.remove(TOKEN_PREFIX + session.getToken().asString());
        for (RefreshTokenValue refreshToken : session.refreshTokenValues()) {
            cache.remove(REFRESH_TOKEN_PREFIX + refreshToken.getToken().asString());
        }
    }

    private long remainingSeconds(PreAuthSession session) {
        return remainingSeconds(session.getExpiredAt());
    }

    private long remainingSeconds(long expiredAt) {
        return Math.max(1L, ttlSeconds(expiredAt));
    }

    private long ttlSeconds(long expiredAt) {
        long remainingMillis = expiredAt - System.currentTimeMillis();
        return remainingMillis <= 0 ? 0L : (remainingMillis + 999L) / 1000L;
    }

    private void removeExpiredActiveSessions() {
        redis().zremrangebyscore(ACTIVE_SESSION_KEY, 0, System.currentTimeMillis());
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

    @PreDestroy
    public void destroy() {
        if (redisConnection != null) {
            redisConnection.close();
        }
        redisClient.shutdown();
    }
}
