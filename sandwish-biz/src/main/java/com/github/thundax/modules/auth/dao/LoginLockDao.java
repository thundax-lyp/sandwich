package com.github.thundax.modules.auth.dao;

/**
 * 迁移兼容：旧账号维度登录锁定端口。
 */
@Deprecated
public interface LoginLockDao {

    Integer getFailCount(String loginName);

    void setFailCount(String loginName, Integer failCount, int expireSeconds);

    void incrementFailCount(String loginName, long delta);

    void deleteFailCount(String loginName);

    boolean isLocked(String loginName);

    Long getLockedExpire(String loginName);

    void lock(String loginName, int lockSeconds);

    void unlock(String loginName);
}
