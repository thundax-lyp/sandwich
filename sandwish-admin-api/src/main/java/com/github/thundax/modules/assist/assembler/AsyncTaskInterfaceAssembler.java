package com.github.thundax.modules.assist.assembler;

import com.github.thundax.modules.assist.controller.response.AsyncTaskResponse;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.entity.valueobject.AsyncTaskIdCodec;
import org.springframework.lang.NonNull;

public final class AsyncTaskInterfaceAssembler {
    private AsyncTaskInterfaceAssembler() {}

    @NonNull
    public static AsyncTaskResponse toResponse(AsyncTask entity) {
        if (entity == null) {
            return AsyncTaskResponse.builder().build();
        }
        return AsyncTaskResponse.builder()
                .id(AsyncTaskIdCodec.toValue(entity.getId()))
                .remarks(entity.getRemarks())
                .priority(entity.getPriority())
                .status(entity.getStatus().value())
                .message(entity.getMessage())
                .data(entity.getData())
                .build();
    }
}
