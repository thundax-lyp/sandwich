package com.github.thundax.modules.member.service.query;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.entity.valueobject.MemberId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberQuery {
    private List<MemberId> ids;
    private MemberStatus status;
    private String name;
    private String remarks;
    private SortDirection sortDirection = SortDirection.ASC;
}
