package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.request.DictSaveRequest;
import com.github.thundax.modules.sys.response.DictResponse;
import org.springframework.lang.NonNull;

public class DictInterfaceAssembler {
    public EntityId toEntityId(String id) {
        return EntityIdCodec.toDomain(id);
    }

    @NonNull
    public DictResponse toResponse(Dict entity) {
        if (entity == null) {
            return new DictResponse();
        }
        DictResponse response = new DictResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        response.setLabel(entity.getLabel());
        response.setType(entity.getType());
        response.setValue(entity.getValue());
        return response;
    }

    @NonNull
    public Dict toEntity(@NonNull Dict entity, @NonNull DictSaveRequest request) {
        entity.setId(EntityIdCodec.toDomain(request.getId()));
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        entity.setLabel(request.getLabel());
        entity.setType(request.getType());
        entity.setValue(request.getValue());
        entity.setRemarks(request.getRemarks());
        return entity;
    }
}
