package com.github.thundax.modules.assist.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.assist.controller.response.AsyncTaskResponse;
import com.github.thundax.modules.assist.entity.AsyncTask;
import org.springframework.lang.NonNull;

public final class AsyncTaskInterfaceAssembler {
    private AsyncTaskInterfaceAssembler() {}

    @NonNull
    public static AsyncTaskResponse toResponse(AsyncTask entity) {
        if (entity == null) {
            return AsyncTaskResponse.builder().build();
        }
        return AsyncTaskResponse.builder()
                .id(EntityIdCodec.toValue(entity.getId()))
                .remarks(entity.getRemarks())
                .priority(entity.getPriority())
                .status(entity.getStatus().value())
                .message(entity.getMessage())
                .data(entity.getData())
                .build();
    }
}
