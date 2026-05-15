package com.github.thundax.modules.submission.persistence.assembler;

import static org.junit.Assert.*;

import com.github.thundax.modules.submission.entity.Submission;
import com.github.thundax.modules.submission.entity.enums.SubmissionStatus;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import com.github.thundax.modules.submission.persistence.dataobject.SubmissionDO;
import java.util.Date;
import org.junit.Test;

public class SubmissionPersistenceAssemblerTest {

    @Test
    public void shouldMapStatusAtPersistenceBoundary() {
        SubmissionDO dataObject = new SubmissionDO();
        dataObject.setStatus("SUBMITTED");

        Submission entity = SubmissionPersistenceAssembler.toEntity(dataObject);

        assertSame(SubmissionStatus.SUBMITTED, entity.getStatus());
        entity.setStatus(SubmissionStatus.APPROVED);
        assertEquals(
                "APPROVED", SubmissionPersistenceAssembler.toDataObject(entity).getStatus());
    }

    @Test
    public void shouldMapSubmissionFields() {
        Date submittedAt = new Date(1000L);
        Submission entity = new Submission();
        entity.setId(SubmissionId.of(9001L));
        entity.setTitle("title");
        entity.setContent("content");
        entity.setSourceClientId("client-1");
        entity.setStatus(SubmissionStatus.REJECTED);
        entity.setPriority(20);
        entity.setSubmittedAt(submittedAt);

        SubmissionDO dataObject = SubmissionPersistenceAssembler.toDataObject(entity);

        assertEquals(Long.valueOf(9001L), dataObject.getId());
        assertEquals("title", dataObject.getTitle());
        assertEquals("content", dataObject.getContent());
        assertEquals("client-1", dataObject.getSourceClientId());
        assertEquals("REJECTED", dataObject.getStatus());
        assertEquals(Integer.valueOf(20), dataObject.getPriority());
        assertSame(submittedAt, dataObject.getSubmittedAt());
    }

    @Test
    public void shouldNormalizeNegativePriorityAtPersistenceBoundary() {
        Submission entity = new Submission();
        entity.setPriority(-1);
        SubmissionDO dataObject = new SubmissionDO();
        dataObject.setPriority(-1);

        assertEquals(
                Integer.valueOf(0),
                SubmissionPersistenceAssembler.toDataObject(entity).getPriority());
        assertEquals(0, SubmissionPersistenceAssembler.toEntity(dataObject).getPriority());
    }
}
