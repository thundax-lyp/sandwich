package com.github.thundax.modules.submission.persistence.assembler;

import com.github.thundax.modules.submission.entity.Submission;
import com.github.thundax.modules.submission.entity.enums.SubmissionStatus;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionIdCodec;
import com.github.thundax.modules.submission.persistence.dataobject.SubmissionDO;
import java.util.ArrayList;
import java.util.List;

public final class SubmissionPersistenceAssembler {

    private SubmissionPersistenceAssembler() {}

    public static SubmissionDO toDataObject(Submission entity) {
        if (entity == null) {
            return null;
        }
        SubmissionDO dataObject = new SubmissionDO();
        dataObject.setId(SubmissionIdCodec.toValue(entity.getId()));
        dataObject.setTitle(entity.getTitle());
        dataObject.setContent(entity.getContent());
        dataObject.setSourceClientId(entity.getSourceClientId());
        dataObject.setStatus(statusValue(entity.getStatus()));
        dataObject.setPriority(priorityOrDefault(entity.getPriority()));
        dataObject.setSubmittedAt(entity.getSubmittedAt());
        dataObject.setLastStatusChangedAt(entity.getLastStatusChangedAt());
        return dataObject;
    }

    public static Submission toEntity(SubmissionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Submission entity = new Submission();
        entity.setId(SubmissionIdCodec.toDomain(dataObject.getId()));
        entity.setTitle(dataObject.getTitle());
        entity.setContent(dataObject.getContent());
        entity.setSourceClientId(dataObject.getSourceClientId());
        entity.setStatus(statusFrom(dataObject.getStatus()));
        entity.setPriority(priorityOrDefault(dataObject.getPriority()));
        entity.setSubmittedAt(dataObject.getSubmittedAt());
        entity.setLastStatusChangedAt(dataObject.getLastStatusChangedAt());
        return entity;
    }

    public static List<Submission> toEntityList(List<SubmissionDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Submission> entities = new ArrayList<>();
        for (SubmissionDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static int priorityOrDefault(Integer priority) {
        return priority == null || priority < 0 ? 0 : priority;
    }

    private static String statusValue(SubmissionStatus status) {
        return status == null ? null : status.value();
    }

    private static SubmissionStatus statusFrom(String status) {
        return status == null ? null : SubmissionStatus.from(status);
    }
}
