package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.modules.auth.service.UserAccessService;
import org.springframework.stereotype.Service;

@Service
public class UserAccessServiceImpl implements UserAccessService {

    @Override
    public String getCurrentUserId() {
        return null;
    }

    @Override
    public Object getSessionCache(String name) {
        return null;
    }

    @Override
    public void setSessionCache(String name, Object value) {}
}
