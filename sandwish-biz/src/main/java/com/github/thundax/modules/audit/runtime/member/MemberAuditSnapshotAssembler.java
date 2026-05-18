package com.github.thundax.modules.audit.runtime.member;

import com.github.thundax.modules.audit.entity.valueobject.AuditField;
import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import com.github.thundax.modules.audit.runtime.AuditSnapshotAssembler;
import com.github.thundax.modules.audit.runtime.AuditSnapshots;
import com.github.thundax.modules.member.entity.Member;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MemberAuditSnapshotAssembler implements AuditSnapshotAssembler {

    @Override
    public String objectType() {
        return "Member";
    }

    @Override
    public String objectTypeLabel() {
        return "会员";
    }

    @Override
    public List<AuditField> fields() {
        return Arrays.asList(
                AuditSnapshots.field("name", "名称", null),
                AuditSnapshots.field("status", "状态", null),
                AuditSnapshots.field("gender", "性别", null));
    }

    @Override
    public AuditSnapshot assemble(Object object) {
        Member member = (Member) object;
        if (member == null) {
            return null;
        }
        return AuditSnapshots.of(
                objectType(),
                member.getId(),
                member.getName(),
                AuditSnapshots.field("name", "名称", member.getName()),
                AuditSnapshots.field("status", "状态", member.getStatus()),
                AuditSnapshots.field("gender", "性别", member.getGender()));
    }
}
