package com.github.thundax.modules.member.service;

import com.fasterxml.jackson.core.type.TypeReference;

/** @author wdit */
public interface SessionCacheService {

    /**
     * 获取缓存对象
     *
     * @param sessionId sessionId
     * @param key key
     * @param clazz clazz
     * @param <T> T
     * @return 对象
     */
    <T> T get(String sessionId, String key, Class<T> clazz);

    /**
     * 获取缓存对象
     *
     * @param sessionId sessionId
     * @param key key
     * @param type type
     * @param <T> T
     * @return 对象
     */
    <T> T get(String sessionId, String key, TypeReference<T> type);

    /**
     * 写入缓存对象
     *
     * @param sessionId sessionId
     * @param key key
     * @param value value
     * @param expireSeconds expireSeconds
     */
    void put(String sessionId, String key, Object value, int expireSeconds);

    /**
     * 删除缓存对象
     *
     * @param sessionId sessionId
     * @param key key
     */
    void remove(String sessionId, String key);
}
