package com.github.thundax.modules.assist.persistence.dao;

import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.assist.dao.KeypairPrivateKeyDao;
import org.springframework.stereotype.Repository;

/**
 * 公私钥私钥临时存储 Redis DAO 实现。
 */
@Repository
public class KeypairPrivateKeyDaoImpl implements KeypairPrivateKeyDao {

    private final RedisClient redisClient;

    public KeypairPrivateKeyDaoImpl(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public void save(String token, String privateKey, int expiredSeconds) {
        redisClient.set(Constants.CACHE_PRIVATE_KEY_ + token, privateKey, expiredSeconds);
    }

    @Override
    public String getByToken(String token) {
        return redisClient.get(Constants.CACHE_PRIVATE_KEY_ + token);
    }
}
