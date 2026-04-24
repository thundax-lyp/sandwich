package com.github.thundax.modules.member.security;

import com.github.thundax.modules.member.entity.Member;

import java.io.Serializable;

/**
 * @author wdit
 */
public class MemberPrincipal implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;

    public MemberPrincipal(String id) {
        this.id = id;
    }

    public MemberPrincipal(Member member) {
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
