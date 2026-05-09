package com.github.thundax.modules.audit.runtime.member;

import com.github.thundax.modules.audit.runtime.AuditObjectLoader;
import com.github.thundax.modules.member.entity.valueobject.MemberIdCodec;
import com.github.thundax.modules.member.service.MemberService;
import org.springframework.stereotype.Component;

@Component
public class MemberAuditObjectLoader implements AuditObjectLoader {

    private final MemberService memberService;

    public MemberAuditObjectLoader(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public String objectType() {
        return "Member";
    }

    @Override
    public Object load(String objectId) {
        return memberService.get(MemberIdCodec.toDomain(Long.valueOf(objectId)));
    }
}
