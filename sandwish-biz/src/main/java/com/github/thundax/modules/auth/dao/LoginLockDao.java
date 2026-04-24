package com.github.thundax.modules.auth.dao;

/**
 * 登录失败锁定 DAO。
 */
public interface LoginLockDao {

    /**
     * 获取失败次数。
     *
     * @param loginName 登录名
     * @return 失败次数
     */
    Integer getFailCount(String loginName);

    /**
     * 保存失败次数。
     *
     * @param loginName 登录名
     * @param failCount 失败次数
     * @param expireSeconds 过期时间
     */
    void setFailCount(String loginName, Integer failCount, int expireSeconds);

    /**
     * 失败次数自增。
     *
     * @param loginName 登录名
     * @param delta 增量
     */
    void incrementFailCount(String loginName, long delta);

    /**
     * 删除失败次数。
     *
     * @param loginName 登录名
     */
    void deleteFailCount(String loginName);

    /**
     * 是否锁定。
     *
     * @param loginName 登录名
     * @return true if locked
     */
    boolean isLocked(String loginName);

    /**
     * 获取锁定剩余时间。
     *
     * @param loginName 登录名
     * @return 剩余时间
     */
    Long getLockedExpire(String loginName);

    /**
     * 锁定用户。
     *
     * @param loginName 登录名
     * @param lockSeconds 锁定时间
     */
    void lock(String loginName, int lockSeconds);

    /**
     * 解除锁定。
     *
     * @param loginName 登录名
     */
    void unlock(String loginName);
}
