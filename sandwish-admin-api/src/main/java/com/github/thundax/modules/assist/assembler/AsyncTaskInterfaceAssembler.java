package com.github.thundax.modules.assist.assembler;

import com.github.thundax.common.persistence.DataEntity;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.response.AsyncTaskResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class AsyncTaskInterfaceAssembler {

    @NonNull
    public AsyncTaskResponse toResponse(AsyncTask entity) {
        if (entity == null) {
            return new AsyncTaskResponse();
        }

        AsyncTaskResponse response = baseEntityToResponse(new AsyncTaskResponse(), entity);
        response.setStatus(entity.getStatus());
        response.setMessage(entity.getMessage());
        response.setData(entity.getData());
        return response;
    }

    private static AsyncTaskResponse baseEntityToResponse(
            AsyncTaskResponse response, DataEntity entity) {
        response.setId(entity.getId());
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        return response;
    }
}
