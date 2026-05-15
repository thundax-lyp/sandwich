package com.github.thundax.modules.submission.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.submission.entity.Submission;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import java.util.Date;
import java.util.List;

public interface SubmissionDao {

    Submission getById(SubmissionId id);

    List<Submission> listByIds(List<Long> idList);

    List<Submission> list(
            String status,
            String sourceClientId,
            Date submittedAtBegin,
            Date submittedAtEnd,
            SortDirection sortDirection);

    Page<Submission> page(
            String status,
            String sourceClientId,
            Date submittedAtBegin,
            Date submittedAtEnd,
            SortDirection sortDirection,
            int pageNo,
            int pageSize);

    int maxPriority();

    SubmissionId insert(Submission entity);

    int updateStatus(Submission entity);

    int updatePriority(SubmissionId id, int priority);

    int deleteById(SubmissionId id);
}
