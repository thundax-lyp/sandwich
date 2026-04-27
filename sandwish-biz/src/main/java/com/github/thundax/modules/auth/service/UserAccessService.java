package com.github.thundax.modules.auth.service;

/** @author wdit */
public interface UserAccessService {

    /**
     * 获取当前用户id
     *
     * @return 用户id
     */
    String getCurrentUserId();

    /**
     * 获取session缓存对象
     *
     * @param name 名称
     * @return 缓存对象
     */
    Object getSessionCache(String name);

    /**
     * 保存session缓存对象
     *
     * @param name 名称
     * @param value 对象
     */
    void setSessionCache(String name, Object value);
}
