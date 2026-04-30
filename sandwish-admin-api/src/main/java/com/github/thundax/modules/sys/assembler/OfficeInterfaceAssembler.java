package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.request.OfficeSaveRequest;
import com.github.thundax.modules.sys.response.OfficeResponse;
import com.github.thundax.modules.sys.service.OfficeService;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class OfficeInterfaceAssembler {

    private final OfficeService officeService;

    public OfficeInterfaceAssembler(OfficeService officeService) {
        this.officeService = officeService;
    }

    public EntityId toEntityId(String id) {
        return EntityIdCodec.toDomain(id);
    }

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
        response.setNamePath(namePath(entity));

        return response;
    }

    @NonNull
    public OfficeResponse toTreeResponse(Office entity) {
        if (entity == null) {
            return new OfficeResponse();
        }

        OfficeResponse response = new OfficeResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
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

    private static OfficeResponse baseEntityToResponse(OfficeResponse response, Office entity) {
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        return response;
    }

    private static Office baseRequestToEntity(Office entity, OfficeSaveRequest request) {
        entity.setId(EntityIdCodec.toDomain(request.getId()));
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        return entity;
    }

    private String namePath(Office office) {
        List<String> names = new ArrayList<>();
        Office node = office;
        while (node != null && EntityIdCodec.toValue(node.getId()) != null) {
            node = officeService.get(node.getId());
            if (node != null) {
                names.add(0, node.getName());
                node = officeService.get(EntityIdCodec.toDomain(node.getParentId()));
            }
        }
        return StringUtils.join(names, "/");
    }
}
