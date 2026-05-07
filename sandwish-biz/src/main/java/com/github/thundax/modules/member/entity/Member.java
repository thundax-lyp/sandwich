package com.github.thundax.modules.member.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.enums.MemberGender;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member implements Auditable, Sortable {
    private EntityId id;

    private String name;
    private MemberGender gender;

    private MemberStatus status = MemberStatus.ACTIVE;

    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public boolean isActive() {
        return MemberStatus.ACTIVE == getStatus();
    }
}
