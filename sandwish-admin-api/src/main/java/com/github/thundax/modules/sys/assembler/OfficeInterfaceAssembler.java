package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.persistence.DataEntity;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.request.OfficeSaveRequest;
import com.github.thundax.modules.sys.response.OfficeResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class OfficeInterfaceAssembler {

    @NonNull
    public OfficeResponse toResponse(Office entity) {
        if (entity == null) {
            return new OfficeResponse();
        }

        OfficeResponse response = baseEntityToResponse(new OfficeResponse(), entity);

        if (StringUtils.isNotEmpty(entity.getParentId())) {
            response.setParentId(entity.getParentId());
        }
        response.setName(entity.getName());
        response.setShortName(entity.getShortName());
        response.setNamePath(entity.getNamePath());

        return response;
    }

    @NonNull
    public OfficeResponse toTreeResponse(Office entity) {
        if (entity == null) {
            return new OfficeResponse();
        }

        OfficeResponse response = new OfficeResponse();
        response.setId(entity.getId());
        if (StringUtils.isNotBlank(entity.getParentId())) {
            response.setParentId(entity.getParentId());
        }
        response.setName(entity.getName());
        response.setShortName(entity.getShortName());
        return response;
    }

    @NonNull
    public Office toEntity(@NonNull Office entity, @NonNull OfficeSaveRequest request) {
        baseRequestToEntity(entity, request);

        if (StringUtils.isNotEmpty(request.getParentId())) {
            entity.setParentId(request.getParentId());
        }
        entity.setName(request.getName());
        entity.setShortName(request.getShortName());

        return entity;
    }

    private static OfficeResponse baseEntityToResponse(OfficeResponse response, DataEntity entity) {
        response.setId(entity.getId());
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        return response;
    }

    private static <T extends DataEntity<T>> T baseRequestToEntity(T entity, OfficeSaveRequest request) {
        entity.setId(request.getId());
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        return entity;
    }
}
