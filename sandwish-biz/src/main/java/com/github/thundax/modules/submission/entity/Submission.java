package com.github.thundax.modules.submission.entity;

import com.github.thundax.common.domain.Sortable;
import com.github.thundax.modules.submission.entity.enums.SubmissionStatus;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import java.util.ArrayList;
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
public class Submission implements Sortable {
    private SubmissionId id;

    private String title;
    private String content;
    private String sourceClientId;

    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    private int priority;
    private Date submittedAt;
    private Date lastStatusChangedAt;
    private List<SubmissionImage> images = new ArrayList<>();
}
