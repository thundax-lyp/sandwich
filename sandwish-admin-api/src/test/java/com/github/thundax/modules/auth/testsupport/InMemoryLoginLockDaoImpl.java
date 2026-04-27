package com.github.thundax.modules.auth.testsupport;

import com.github.thundax.modules.auth.dao.LoginLockDao;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.Map;

@Profile("test")
@Repository
public class InMemoryLoginLockDaoImpl implements LoginLockDao {

    private final Map<String, Integer> failCounts = new LinkedHashMap<>();
    private final Map<String, Integer> lockedSeconds = new LinkedHashMap<>();

    @Override
    public Integer getFailCount(String loginName) {
        return failCounts.get(loginName);
    }

    @Override
    public void setFailCount(String loginName, Integer failCount, int expireSeconds) {
        failCounts.put(loginName, failCount);
    }

    @Override
    public void incrementFailCount(String loginName, long delta) {
        failCounts.put(loginName, (int) (failCounts.getOrDefault(loginName, 0) + delta));
    }

    @Override
    public void deleteFailCount(String loginName) {
        failCounts.remove(loginName);
    }

    @Override
    public boolean isLocked(String loginName) {
        return lockedSeconds.containsKey(loginName);
    }

    @Override
    public Long getLockedExpire(String loginName) {
        Integer expire = lockedSeconds.get(loginName);
        return expire == null ? null : expire.longValue();
    }

    @Override
    public void lock(String loginName, int lockSeconds) {
        lockedSeconds.put(loginName, lockSeconds);
    }

    @Override
    public void unlock(String loginName) {
        lockedSeconds.remove(loginName);
    }
}
