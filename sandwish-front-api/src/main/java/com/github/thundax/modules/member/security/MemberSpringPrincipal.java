package com.github.thundax.modules.member.security;

import com.github.thundax.modules.member.entity.Member;
import java.io.Serializable;


public class MemberSpringPrincipal implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;

    public MemberSpringPrincipal(String id) {
        this.id = id;
    }

    public MemberSpringPrincipal(Member member) {
        this.id = member.getId();
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
