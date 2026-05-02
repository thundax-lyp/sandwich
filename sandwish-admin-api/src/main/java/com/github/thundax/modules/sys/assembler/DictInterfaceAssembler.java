package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.controller.request.DictPageRequest;
import com.github.thundax.modules.sys.controller.request.DictQueryRequest;
import com.github.thundax.modules.sys.controller.request.DictSaveRequest;
import com.github.thundax.modules.sys.controller.response.DictResponse;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.query.DictQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class DictInterfaceAssembler {
    private DictInterfaceAssembler() {}

    @NonNull
    public static DictResponse toResponse(Dict entity) {
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
    public static DictQuery toQuery(@NonNull DictQueryRequest request) {
        DictQuery query = new DictQuery();
        query.setLabel(emptyToNull(request.getLabel()));
        query.setType(emptyToNull(request.getType()));
        query.setRemarks(emptyToNull(request.getRemarks()));
        return query;
    }

    @NonNull
    public static DictQuery toQuery(@NonNull DictPageRequest request) {
        DictQuery query = new DictQuery();
        query.setLabel(emptyToNull(request.getLabel()));
        query.setType(emptyToNull(request.getType()));
        query.setRemarks(emptyToNull(request.getRemarks()));
        return query;
    }

    @NonNull
    public static Dict toEntity(@NonNull Dict entity, @NonNull DictSaveRequest request) {
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

    private static String emptyToNull(String value) {
        return StringUtils.isEmpty(value) ? null : value;
    }
}
