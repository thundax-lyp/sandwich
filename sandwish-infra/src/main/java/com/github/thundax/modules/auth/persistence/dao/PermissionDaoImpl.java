package com.github.thundax.modules.auth.persistence.dao;

import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.auth.dao.PermissionDao;
import com.github.thundax.modules.auth.entity.PermissionSession;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/** 后台权限会话 Redis DAO 实现。 */
@Repository
@Profile("!test")
public class PermissionDaoImpl implements PermissionDao {

    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "AUTH_PERMISSION_";

    private static final String TOKEN_PREFIX = CACHE_SECTION + "TOKEN_";

    private final RedisClient redisClient;

    public PermissionDaoImpl(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public PermissionSession getByToken(String token) {
        return redisClient.get(TOKEN_PREFIX + token, PermissionSession.class);
    }

    @Override
    public void save(PermissionSession session, int expiredSeconds) {
        Assert.hasText(session.getToken(), "token can not be empty");
        redisClient.set(TOKEN_PREFIX + session.getToken(), session, expiredSeconds);
    }

    @Override
    public void touch(String token, int expiredSeconds) {
        redisClient.expire(TOKEN_PREFIX + token, expiredSeconds);
    }

    @Override
    public void deleteByToken(String token) {
        redisClient.delete(TOKEN_PREFIX + token);
    }

    @Override
    public void deleteAll() {
        redisClient.deleteByPattern(TOKEN_PREFIX);
    }
}
