package com.github.thundax.modules.submission.service.query;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.submission.entity.enums.SubmissionStatus;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionQuery {
    private List<SubmissionId> ids;
    private SubmissionStatus status;
    private Date submittedAtBegin;
    private Date submittedAtEnd;
    private SortDirection sortDirection = SortDirection.ASC;
}
