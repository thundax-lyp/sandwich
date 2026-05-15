package com.github.thundax.modules.submission.service.command;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionSortCommand {
    private List<SubmissionId> orderedIds;
    private SortDirection sortDirection;
}
