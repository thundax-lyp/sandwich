package com.github.thundax.modules.member.service.command;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberCommand {
    private EntityId id;
    private Member member;
}
