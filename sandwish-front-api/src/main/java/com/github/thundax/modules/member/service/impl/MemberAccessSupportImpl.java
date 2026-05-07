package com.github.thundax.modules.member.service.impl;

import com.github.thundax.modules.member.security.MemberSecurityContext;
import com.github.thundax.modules.member.service.MemberAccessSupport;
import org.springframework.stereotype.Service;

@Service
public class MemberAccessSupportImpl implements MemberAccessSupport {

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
