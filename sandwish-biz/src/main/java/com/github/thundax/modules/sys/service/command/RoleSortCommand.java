package com.github.thundax.modules.sys.service.command;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.sys.entity.valueobject.RoleId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleSortCommand {

    private List<RoleId> orderedIds;
    private SortDirection sortDirection;
}
