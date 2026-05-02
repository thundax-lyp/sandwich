package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.request.OfficeQueryRequest;
import com.github.thundax.modules.sys.request.OfficeSaveRequest;
import com.github.thundax.modules.sys.response.OfficeResponse;
import com.github.thundax.modules.sys.service.query.OfficeQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class OfficeInterfaceAssembler {
    private OfficeInterfaceAssembler() {}

    @NonNull
    public static OfficeResponse toResponse(Office entity, Function<EntityId, Office> officeLoader) {
        if (entity == null) {
            return new OfficeResponse();
        }

        OfficeResponse response = new OfficeResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        if (StringUtils.isNotEmpty(entity.getParentId())) {
            response.setParentId(entity.getParentId());
        }
        response.setName(entity.getName());
        response.setShortName(entity.getShortName());
        response.setNamePath(namePath(entity, officeLoader));
        return response;
    }

    @NonNull
    public static OfficeResponse toTreeResponse(Office entity) {
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
    public static OfficeQuery toQuery(@NonNull OfficeQueryRequest request) {
        OfficeQuery query = new OfficeQuery();
        query.setParentId(request.getParentId());
        query.setName(request.getName());
        query.setRemarks(request.getRemarks());
        return query;
    }

    @NonNull
    public static Office toEntity(@NonNull Office entity, @NonNull OfficeSaveRequest request) {
        entity.setId(EntityIdCodec.toDomain(request.getId()));
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        if (StringUtils.isNotEmpty(request.getParentId())) {
            entity.setParentId(request.getParentId());
        }
        entity.setName(request.getName());
        entity.setShortName(request.getShortName());
        return entity;
    }

    private static String namePath(Office office, Function<EntityId, Office> officeLoader) {
        List<String> names = new ArrayList<>();
        Office node = office;
        while (node != null && EntityIdCodec.toValue(node.getId()) != null) {
            node = officeLoader.apply(node.getId());
            if (node != null) {
                names.add(0, node.getName());
                node = officeLoader.apply(EntityIdCodec.toDomain(node.getParentId()));
            }
        }
        return StringUtils.join(names, "/");
    }
}
