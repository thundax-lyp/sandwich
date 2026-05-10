package com.github.thundax.modules.member.service.command;

import com.github.thundax.common.domain.SortDirection;
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
public class MemberSortCommand {

    private List<MemberId> orderedIds;
    private SortDirection sortDirection;
}
