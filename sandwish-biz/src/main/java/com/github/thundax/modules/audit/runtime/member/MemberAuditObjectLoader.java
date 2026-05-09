package com.github.thundax.modules.audit.runtime.member;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.audit.runtime.AuditObjectLoader;
import com.github.thundax.modules.member.service.MemberService;
import com.github.thundax.modules.member.service.query.MemberQuery;
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
        MemberQuery query = new MemberQuery();
        query.setId(EntityIdCodec.toDomain(Long.valueOf(objectId)));
        return memberService.get(query);
    }
}
