package com.github.thundax.modules.member.support.impl;

import com.github.thundax.modules.member.security.MemberSecurityContext;
import com.github.thundax.modules.member.support.MemberAccessSupport;
import org.springframework.stereotype.Component;

@Component
public class MemberAccessSupportImpl implements MemberAccessSupport {

    @Override
    public String getCurrentMemberId() {
        return MemberSecurityContext.getCurrentMemberId();
    }
}
