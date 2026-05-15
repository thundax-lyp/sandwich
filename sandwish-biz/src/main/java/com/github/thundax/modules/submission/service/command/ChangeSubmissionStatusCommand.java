package com.github.thundax.modules.submission.service.command;

import com.github.thundax.modules.submission.entity.enums.SubmissionStatus;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeSubmissionStatusCommand {
    private SubmissionId id;
    private SubmissionStatus status;
}
