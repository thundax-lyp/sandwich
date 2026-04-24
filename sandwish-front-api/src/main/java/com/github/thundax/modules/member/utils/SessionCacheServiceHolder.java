package com.github.thundax.modules.member.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.member.service.SessionCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wdit
 */
@Service
public class SessionCacheServiceHolder {

    private static SessionCacheService service;

    @Autowired
    public SessionCacheServiceHolder(SessionCacheService targetService) {
        service = targetService;
    }

    public static SessionCacheService getService() {
        if (service == null) {
            service = SpringContextHolder.getBean(SessionCacheService.class);
        }
        return service;
    }

    public static <T> T get(String sessionId, String key, Class<T> clazz) {
        return getService().get(sessionId, key, clazz);
    }

    public static <T> T get(String sessionId, String key, TypeReference<T> type) {
        return getService().get(sessionId, key, type);
    }

    public static void put(String sessionId, String key, Object value, int expireSeconds) {
        getService().put(sessionId, key, value, expireSeconds);
    }

    public static void remove(String sessionId, String key) {
        getService().remove(sessionId, key);
    }
}
