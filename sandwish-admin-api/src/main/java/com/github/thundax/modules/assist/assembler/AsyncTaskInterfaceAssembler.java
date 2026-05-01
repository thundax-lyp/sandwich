package com.github.thundax.modules.assist.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.response.AsyncTaskResponse;
import org.springframework.lang.NonNull;

public final class AsyncTaskInterfaceAssembler {
    private AsyncTaskInterfaceAssembler() {}

    @NonNull
    public static AsyncTaskResponse toResponse(AsyncTask entity) {
        if (entity == null) {
            return new AsyncTaskResponse();
        }
        AsyncTaskResponse response = new AsyncTaskResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        response.setStatus(entity.getStatus().value());
        response.setMessage(entity.getMessage());
        response.setData(entity.getData());
        return response;
    }
}
