package com.github.thundax.modules.auth.support.impl;

import com.github.thundax.modules.auth.security.MemberSecurityContext;
import com.github.thundax.modules.auth.support.MemberAccessSupport;
import org.springframework.stereotype.Component;

@Component
public class MemberAccessSupportImpl implements MemberAccessSupport {

    @Override
    public String getCurrentMemberId() {
        return MemberSecurityContext.getCurrentMemberId();
    }
}
