package com.github.thundax.modules.submission.service;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.submission.entity.Submission;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import com.github.thundax.modules.submission.service.command.ChangeSubmissionStatusCommand;
import com.github.thundax.modules.submission.service.command.CreateSubmissionCommand;
import com.github.thundax.modules.submission.service.command.SubmissionSortCommand;
import com.github.thundax.modules.submission.service.query.SubmissionQuery;
import java.util.List;

public interface SubmissionService {

    Submission get(SubmissionId id);

    List<Submission> list(SubmissionQuery query);

    PageResult<Submission> page(SubmissionQuery query, PageQuery page);

    SubmissionId create(CreateSubmissionCommand command);

    int changeStatus(ChangeSubmissionStatusCommand command);

    void sort(SubmissionSortCommand command);
}
