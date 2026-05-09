package com.github.thundax.modules.auth.security;

import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.valueobject.MemberIdCodec;
import java.io.Serializable;

public class MemberSpringPrincipal implements Serializable {

    private final String id;

    public MemberSpringPrincipal(String id) {
        this.id = id;
    }

    public MemberSpringPrincipal(Member member) {
        this.id = MemberIdCodec.toStringValue(member.getId());
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
