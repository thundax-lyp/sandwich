package com.github.thundax.modules.auth.persistence.dao;

import com.github.thundax.common.Constants;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.auth.dao.LoginLockDao;
import org.springframework.stereotype.Repository;

/**
 * 登录失败锁定 Redis DAO 实现。
 */
@Repository
public class LoginLockDaoImpl implements LoginLockDao {

    private static final String FAIL_COUNT_PREFIX = Constants.CACHE_PREFIX + "login_fail_count_";
    private static final String LOCK_USER_PREFIX = Constants.CACHE_PREFIX + "lock_user_";

    private final RedisClient redisClient;

    public LoginLockDaoImpl(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public Integer getFailCount(String loginName) {
        return redisClient.get(FAIL_COUNT_PREFIX + loginName, Integer.class);
    }

    @Override
    public void setFailCount(String loginName, Integer failCount, int expireSeconds) {
        redisClient.set(FAIL_COUNT_PREFIX + loginName, failCount, expireSeconds);
    }

    @Override
    public void incrementFailCount(String loginName, long delta) {
        redisClient.increment(FAIL_COUNT_PREFIX + loginName, delta);
    }

    @Override
    public void deleteFailCount(String loginName) {
        redisClient.delete(FAIL_COUNT_PREFIX + loginName);
    }

    @Override
    public boolean isLocked(String loginName) {
        return redisClient.exists(LOCK_USER_PREFIX + loginName);
    }

    @Override
    public Long getLockedExpire(String loginName) {
        return redisClient.getExpire(LOCK_USER_PREFIX + loginName);
    }

    @Override
    public void lock(String loginName, int lockSeconds) {
        redisClient.set(LOCK_USER_PREFIX + loginName, Global.YES, lockSeconds);
    }

    @Override
    public void unlock(String loginName) {
        redisClient.delete(LOCK_USER_PREFIX + loginName);
    }
}
