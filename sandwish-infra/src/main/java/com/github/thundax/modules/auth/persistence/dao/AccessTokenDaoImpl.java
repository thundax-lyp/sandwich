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

/** access token Redis DAO 实现。 */
@Repository
@Profile("!test")
@EnableConfigurationProperties(AuthProperties.class)
public class AccessTokenDaoImpl implements AccessTokenDao {

    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "AUTH_ONLINE_";

    /** TOKEN_PREFIX + token : userId */
    private static final String TOKEN_PREFIX = CACHE_SECTION + "TOKEN_";

    /** USER_ID_PREFIX + userId : AccessToken */
    private static final String USER_ID_PREFIX = CACHE_SECTION + "UID_";

    private static final int SAFETY_SECONDS = 5;

    private final AuthProperties properties;
    private final RedisClient redisClient;

    public AccessTokenDaoImpl(RedisClient redisClient, AuthProperties properties) {
        this.redisClient = redisClient;
        this.properties = properties;
    }

    @Override
    public int getOnlineCount() {
        return redisClient.keys(TOKEN_PREFIX).size();
    }

    @Override
    public String getUidByToken(String token) {
        return redisClient.get(TOKEN_PREFIX + token);
    }

    @Override
    public AccessToken getByUserId(String userId) {
        AccessTokenDO accessTokenDO = redisClient.get(USER_ID_PREFIX + userId, AccessTokenDO.class);
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
        redisClient.set(
                TOKEN_PREFIX + accessToken.getToken(), accessToken.getUserId(), expiredSeconds);
        redisClient.set(USER_ID_PREFIX + accessToken.getUserId(), accessTokenDO, expiredSeconds);
    }

    @Override
    public void active(AccessToken accessToken) {
        int expiredSeconds = properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2;

        redisClient.expire(TOKEN_PREFIX + accessToken.getToken(), expiredSeconds);
        redisClient.expire(USER_ID_PREFIX + accessToken.getUserId(), expiredSeconds);
    }

    @Override
    public void delete(AccessToken accessToken) {
        redisClient.delete(TOKEN_PREFIX + accessToken.getToken());
        redisClient.delete(USER_ID_PREFIX + accessToken.getUserId());
    }
}
