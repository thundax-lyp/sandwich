package com.github.thundax.modules.auth.persistence.dao;

import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.AccessTokenDao;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.persistence.assembler.AuthPersistenceAssembler;
import com.github.thundax.modules.auth.persistence.dataobject.AccessTokenDO;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/**
 * access token Redis DAO 实现。
 */
@Repository
@Profile("!test")
@EnableConfigurationProperties(AuthProperties.class)
public class AccessTokenDaoImpl implements AccessTokenDao {

    private static final String CACHE_ = Constants.CACHE_PREFIX + "AUTH_ONLINE_";

    /**
     * CACHE_TOKEN_ + token : userId
     */
    private static final String CACHE_TOKEN_ = CACHE_ + "TOKEN_";

    /**
     * CACHE_USER_ID_ + userId : AccessToken
     */
    private static final String CACHE_USER_ID_ = CACHE_ + "UID_";

    private static final int SAFETY_SECONDS = 5;

    private final AuthProperties properties;
    private final RedisClient redisClient;

    public AccessTokenDaoImpl(RedisClient redisClient, AuthProperties properties) {
        this.redisClient = redisClient;
        this.properties = properties;
    }

    @Override
    public int getOnlineCount() {
        return redisClient.keys(CACHE_TOKEN_).size();
    }

    @Override
    public String getUidByToken(String token) {
        return redisClient.get(CACHE_TOKEN_ + token);
    }

    @Override
    public AccessToken getByUserId(String userId) {
        AccessTokenDO accessTokenDO = redisClient.get(CACHE_USER_ID_ + userId, AccessTokenDO.class);
        AccessToken accessToken = AuthPersistenceAssembler.toEntity(accessTokenDO);
        if (accessToken != null) {
            accessToken.setUserId(userId);
        }
        return accessToken;
    }

    @Override
    public void save(AccessToken accessToken) {
        Assert.notNull(accessToken.getToken(), "token can not be null");
        Assert.notNull(accessToken.getUserId(), "userId can not be null");

        int expiredSeconds = properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2;

        AccessTokenDO accessTokenDO = AuthPersistenceAssembler.toDataObject(accessToken);
        redisClient.set(CACHE_TOKEN_ + accessToken.getToken(), accessToken.getUserId(), expiredSeconds);
        redisClient.set(CACHE_USER_ID_ + accessToken.getUserId(), accessTokenDO, expiredSeconds);
    }

    @Override
    public void active(AccessToken accessToken) {
        int expiredSeconds = properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2;

        redisClient.expire(CACHE_TOKEN_ + accessToken.getToken(), expiredSeconds);
        redisClient.expire(CACHE_USER_ID_ + accessToken.getUserId(), expiredSeconds);
    }

    @Override
    public void delete(AccessToken accessToken) {
        redisClient.delete(CACHE_TOKEN_ + accessToken.getToken());
        redisClient.delete(CACHE_USER_ID_ + accessToken.getUserId());
    }
}
