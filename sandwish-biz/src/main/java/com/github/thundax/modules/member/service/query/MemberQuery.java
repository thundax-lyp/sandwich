package com.github.thundax.modules.member.service.query;

import com.github.thundax.modules.member.entity.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberQuery {
    private MemberStatus status;
    private String name;
    private String remarks;
}
