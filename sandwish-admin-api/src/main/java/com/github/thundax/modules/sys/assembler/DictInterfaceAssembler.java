package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.persistence.DataEntity;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.request.DictSaveRequest;
import com.github.thundax.modules.sys.response.DictResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class DictInterfaceAssembler {

    @NonNull
    public DictResponse toResponse(Dict entity) {
        if (entity == null) {
            return new DictResponse();
        }

        DictResponse response = baseEntityToResponse(new DictResponse(), entity);
        response.setLabel(entity.getLabel());
        response.setType(entity.getType());
        response.setValue(entity.getValue());
        response.setRemarks(entity.getRemarks());
        response.setPriority(entity.getPriority());
        return response;
    }

    @NonNull
    public Dict toEntity(@NonNull Dict entity, @NonNull DictSaveRequest request) {
        baseRequestToEntity(entity, request);
        entity.setLabel(request.getLabel());
        entity.setType(request.getType());
        entity.setValue(request.getValue());
        entity.setRemarks(request.getRemarks());
        entity.setPriority(request.getPriority());
        return entity;
    }

    private static DictResponse baseEntityToResponse(DictResponse response, DataEntity entity) {
        response.setId(entity.getId());
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        return response;
    }

    private static <T extends DataEntity<T>> T baseRequestToEntity(
            T entity, DictSaveRequest request) {
        entity.setId(request.getId());
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        return entity;
    }
}
