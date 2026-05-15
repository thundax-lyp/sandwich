package com.github.thundax.modules.submission.dao;

import com.github.thundax.modules.submission.entity.SubmissionImage;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import java.util.List;

public interface SubmissionImageDao {

    void batchInsert(List<SubmissionImage> images);

    List<SubmissionImage> listBySubmissionId(SubmissionId submissionId);

    int deleteBySubmissionId(SubmissionId submissionId);
}
