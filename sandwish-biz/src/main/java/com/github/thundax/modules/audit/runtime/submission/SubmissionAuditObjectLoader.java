package com.github.thundax.modules.audit.runtime.submission;

import com.github.thundax.modules.audit.runtime.AuditObjectLoader;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionIdCodec;
import com.github.thundax.modules.submission.service.SubmissionService;
import org.springframework.stereotype.Component;

@Component
public class SubmissionAuditObjectLoader implements AuditObjectLoader {

    private static final String OBJECT_TYPE = "Submission";

    private final SubmissionService submissionService;

    public SubmissionAuditObjectLoader(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @Override
    public String objectType() {
        return OBJECT_TYPE;
    }

    @Override
    public Object load(String objectId) {
        return submissionService.get(SubmissionIdCodec.toDomain(objectId));
    }
}
