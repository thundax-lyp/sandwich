package com.github.thundax.modules.audit.runtime.submission;

import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import com.github.thundax.modules.audit.runtime.AuditSnapshotAssembler;
import com.github.thundax.modules.audit.runtime.AuditSnapshots;
import com.github.thundax.modules.submission.entity.Submission;
import org.springframework.stereotype.Component;

@Component
public class SubmissionAuditSnapshotAssembler implements AuditSnapshotAssembler {

    private static final String OBJECT_TYPE = "Submission";

    @Override
    public String objectType() {
        return OBJECT_TYPE;
    }

    @Override
    public AuditSnapshot assemble(Object object) {
        Submission submission = (Submission) object;
        if (submission == null) {
            return null;
        }
        return AuditSnapshots.of(
                objectType(),
                submission.getId(),
                submission.getTitle(),
                AuditSnapshots.field("title", "标题", submission.getTitle()),
                AuditSnapshots.field("content", "正文", submission.getContent()),
                AuditSnapshots.field("status", "状态", submission.getStatus()),
                AuditSnapshots.field("sourceClientId", "来源 client", submission.getSourceClientId()));
    }
}
