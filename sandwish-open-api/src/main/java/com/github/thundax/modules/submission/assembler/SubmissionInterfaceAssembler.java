package com.github.thundax.modules.submission.assembler;

import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.submission.controller.request.SubmissionSaveRequest;
import com.github.thundax.modules.submission.controller.response.SubmissionResponse;
import com.github.thundax.modules.submission.entity.Submission;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionIdCodec;
import com.github.thundax.modules.submission.service.command.CreateSubmissionCommand;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;

public final class SubmissionInterfaceAssembler {

    private SubmissionInterfaceAssembler() {}

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
