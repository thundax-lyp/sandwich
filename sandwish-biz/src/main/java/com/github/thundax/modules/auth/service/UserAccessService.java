package com.github.thundax.modules.auth.service;

public interface UserAccessService {

    String getCurrentUserId();

    Object getSessionCache(String name);

    void setSessionCache(String name, Object value);
}
