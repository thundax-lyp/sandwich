package com.github.thundax.modules.member.service;

import com.fasterxml.jackson.core.type.TypeReference;

public interface SessionCacheService {

    <T> T get(String sessionId, String key, Class<T> clazz);

    <T> T get(String sessionId, String key, TypeReference<T> type);

    void put(String sessionId, String key, Object value, int expireSeconds);

    void remove(String sessionId, String key);
}
