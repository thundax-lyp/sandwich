package com.github.thundax.modules.sys.service.command;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.sys.entity.valueobject.UserId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSortCommand {

    private List<UserId> orderedIds;
    private SortDirection sortDirection;
}
