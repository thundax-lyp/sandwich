package com.github.thundax.modules.member.entity;

import com.github.thundax.common.domain.Sortable;
import com.github.thundax.modules.member.entity.enums.MemberGender;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.entity.valueobject.MemberId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member implements Sortable {
    private MemberId id;

    private String name;
    private MemberGender gender;

    private MemberStatus status = MemberStatus.ACTIVE;

    private int priority;
    private String remarks;

    public boolean isActive() {
        return MemberStatus.ACTIVE == getStatus();
    }
}
