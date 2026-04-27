package com.github.thundax.modules.member.service.impl;

import com.github.thundax.modules.member.security.MemberSecurityContext;
import com.github.thundax.modules.member.service.MemberAccessService;
import org.springframework.stereotype.Service;

/** @author wdit */
@Service
public class MemberAccessServiceImpl implements MemberAccessService {

    @Override
    public String getCurrentMemberId() {
        return MemberSecurityContext.getCurrentMemberId();
    }

    @Override
    public Object getSessionCache(String name) {
        return MemberSecurityContext.getSessionCache(name);
    }

    @Override
    public void setSessionCache(String name, Object value) {
        MemberSecurityContext.setSessionCache(name, value);
    }
}
