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
}
