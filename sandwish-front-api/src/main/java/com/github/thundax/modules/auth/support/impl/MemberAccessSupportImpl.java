package com.github.thundax.modules.auth.support.impl;

import com.github.thundax.modules.auth.security.CurrentMemberResolver;
import com.github.thundax.modules.auth.support.MemberAccessSupport;
import org.springframework.stereotype.Component;

@Component
public class MemberAccessSupportImpl implements MemberAccessSupport {

    private final CurrentMemberResolver currentMemberResolver;

    public MemberAccessSupportImpl(CurrentMemberResolver currentMemberResolver) {
        this.currentMemberResolver = currentMemberResolver;
    }

    @Override
    public String getCurrentMemberId() {
        return currentMemberResolver.currentMemberId();
    }
}
