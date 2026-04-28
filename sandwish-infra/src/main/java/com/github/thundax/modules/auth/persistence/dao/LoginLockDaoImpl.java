package com.github.thundax.modules.auth.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.modules.auth.dao.LoginLockDao;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class LoginLockDaoImpl implements LoginLockDao {

    private static final String FAIL_COUNT_PREFIX = Constants.CACHE_PREFIX + "login_fail_count_";
    private static final String LOCK_USER_PREFIX = Constants.CACHE_PREFIX + "lock_user_";

    @CreateCache(name = Constants.CACHE_PREFIX + "auth.login.lock.", cacheType = CacheType.REMOTE)
    private Cache<String, Object> cache;

    @Override
    public Integer getFailCount(String loginName) {
        return (Integer) cache.get(FAIL_COUNT_PREFIX + loginName);
    }

    @Override
    public void setFailCount(String loginName, Integer failCount, int expireSeconds) {
        cache.put(FAIL_COUNT_PREFIX + loginName, failCount, expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void incrementFailCount(String loginName, long delta) {
        Integer failCount = getFailCount(loginName);
        if (failCount == null) {
            failCount = 0;
        }
        cache.put(FAIL_COUNT_PREFIX + loginName, failCount + (int) delta);
    }

    @Override
    public void deleteFailCount(String loginName) {
        cache.remove(FAIL_COUNT_PREFIX + loginName);
    }

    @Override
    public boolean isLocked(String loginName) {
        Long expireAt = (Long) cache.get(LOCK_USER_PREFIX + loginName);
        return expireAt != null && expireAt > System.currentTimeMillis();
    }

    @Override
    public Long getLockedExpire(String loginName) {
        Long expireAt = (Long) cache.get(LOCK_USER_PREFIX + loginName);
        if (expireAt == null) {
            return null;
        }
        long remainingMillis = expireAt - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            return 0L;
        }
        return TimeUnit.MILLISECONDS.toSeconds(remainingMillis);
    }

    @Override
    public void lock(String loginName, int lockSeconds) {
        long expireAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(lockSeconds);
        cache.put(LOCK_USER_PREFIX + loginName, expireAt, lockSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void unlock(String loginName) {
        cache.remove(LOCK_USER_PREFIX + loginName);
    }
}
