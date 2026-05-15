package com.github.thundax.modules.submission.assembler;

import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.submission.controller.request.SubmissionIdRequest;
import com.github.thundax.modules.submission.controller.request.SubmissionPageRequest;
import com.github.thundax.modules.submission.controller.request.SubmissionSaveRequest;
import com.github.thundax.modules.submission.controller.request.SubmissionStatusRequest;
import com.github.thundax.modules.submission.controller.response.SubmissionResponse;
import com.github.thundax.modules.submission.entity.Submission;
import com.github.thundax.modules.submission.entity.enums.SubmissionStatus;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionIdCodec;
import com.github.thundax.modules.submission.service.command.ChangeSubmissionStatusCommand;
import com.github.thundax.modules.submission.service.command.CreateSubmissionCommand;
import com.github.thundax.modules.submission.service.query.SubmissionQuery;
import java.util.Collections;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class SubmissionInterfaceAssembler {

    private SubmissionInterfaceAssembler() {}

    public static SubmissionId toId(@NonNull SubmissionIdRequest request) {
        return SubmissionIdCodec.toDomain(request.getId());
    }

    @NonNull
    public static SubmissionQuery toQuery(@NonNull SubmissionPageRequest request) {
        SubmissionQuery query = new SubmissionQuery();
        query.setStatus(StringUtils.isBlank(request.getStatus()) ? null : SubmissionStatus.from(request.getStatus()));
        query.setSubmittedAtBegin(request.getSubmittedAtBegin());
        query.setSubmittedAtEnd(request.getSubmittedAtEnd());
        query.setSortDirection(request.getSortDirection());
        return query;
    }

    @NonNull
    public static ChangeSubmissionStatusCommand toChangeStatusCommand(@NonNull SubmissionStatusRequest request) {
        ChangeSubmissionStatusCommand command = new ChangeSubmissionStatusCommand();
        command.setId(SubmissionIdCodec.toDomain(request.getId()));
        command.setStatus(SubmissionStatus.from(request.getStatus()));
        return command;
    }

    @NonNull
    public static CreateSubmissionCommand toCreateCommand(@NonNull SubmissionSaveRequest request) {
        CreateSubmissionCommand command = new CreateSubmissionCommand();
        command.setTitle(request.getTitle());
        command.setContent(request.getContent());
        command.setImageObjectIds(
                request.getImageObjectIds() == null
                        ? Collections.emptyList()
                        : request.getImageObjectIds().stream()
                                .map(StoredObjectIdCodec::toDomain)
                                .collect(Collectors.toList()));
        return command;
    }

    @NonNull
    public static SubmissionResponse toResponse(Submission entity) {
        if (entity == null) {
            return SubmissionResponse.builder()
                    .imageObjectIds(Collections.emptyList())
                    .build();
        }
        return SubmissionResponse.builder()
                .id(SubmissionIdCodec.toStringValue(entity.getId()))
                .title(entity.getTitle())
                .content(entity.getContent())
                .status(entity.getStatus() == null ? null : entity.getStatus().value())
                .submittedAt(entity.getSubmittedAt())
                .imageObjectIds(
                        entity.getImages() == null
                                ? Collections.emptyList()
                                : entity.getImages().stream()
                                        .map(image -> StoredObjectIdCodec.toStringValue(image.getStorageObjectId()))
                                        .collect(Collectors.toList()))
                .build();
    }
}
