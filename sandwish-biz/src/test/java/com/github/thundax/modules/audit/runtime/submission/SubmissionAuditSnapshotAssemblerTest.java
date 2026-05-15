package com.github.thundax.modules.audit.runtime.submission;

import static org.junit.Assert.assertEquals;

import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import com.github.thundax.modules.submission.entity.Submission;
import com.github.thundax.modules.submission.entity.enums.SubmissionStatus;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import org.junit.Test;

public class SubmissionAuditSnapshotAssemblerTest {

    @Test
    public void shouldAssembleSubmissionSnapshot() {
        Submission submission = new Submission();
        submission.setId(SubmissionId.of(9001L));
        submission.setTitle("title");
        submission.setContent("content");
        submission.setStatus(SubmissionStatus.SUBMITTED);

        AuditSnapshot snapshot = new SubmissionAuditSnapshotAssembler().assemble(submission);

        assertEquals("Submission", snapshot.getObjectType());
        assertEquals("9001", snapshot.getObjectId());
        assertEquals("title", snapshot.getDisplayName());
        assertEquals(3, snapshot.getFields().size());
    }
}
