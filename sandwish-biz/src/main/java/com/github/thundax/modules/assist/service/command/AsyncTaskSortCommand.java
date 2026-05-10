package com.github.thundax.modules.assist.service.command;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.assist.entity.valueobject.AsyncTaskId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskSortCommand {

    private List<AsyncTaskId> orderedIds;
    private SortDirection sortDirection;
}
