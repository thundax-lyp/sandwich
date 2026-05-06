package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.codec.AccessRankCodec;
import com.github.thundax.modules.sys.controller.request.MenuQueryRequest;
import com.github.thundax.modules.sys.controller.request.MenuSaveRequest;
import com.github.thundax.modules.sys.controller.response.MenuResponse;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class MenuInterfaceAssembler {
    private MenuInterfaceAssembler() {}

    @NonNull
    public static MenuResponse toResponse(Menu entity) {
        if (entity == null) {
            return new MenuResponse();
        }
        MenuResponse response = new MenuResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        String parentId = EntityIdCodec.toValue(entity.getParentId());
        if (StringUtils.isNotEmpty(parentId)) {
            response.setParentId(parentId);
        }
        response.setName(entity.getName());
        response.setPerms(entity.getPerms());
        response.setRanks(AccessRankCodec.toValue(entity.getRank()));
        response.setDisplay(entity.isDisplay());
        response.setDisplayParams(entity.getDisplayParams());
        response.setUrl(entity.getUrl());
        return response;
    }

    @NonNull
    public static MenuResponse toTreeResponse(Menu entity) {
        if (entity == null) {
            return new MenuResponse();
        }
        MenuResponse response = new MenuResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setParentId(EntityIdCodec.toValue(entity.getParentId()));
        response.setName(entity.getName());
        return response;
    }

    @NonNull
    public static MenuQuery toQuery(@NonNull MenuQueryRequest request) {
        MenuQuery query = new MenuQuery();
        query.setParentId(request.getParentId());
        if (request.getDisplay() != null) {
            query.setVisibility(request.getDisplay() ? MenuVisibility.VISIBLE : MenuVisibility.HIDDEN);
        }
        return query;
    }

    @NonNull
    public static Menu toEntity(@NonNull Menu entity, @NonNull MenuSaveRequest request) {
        entity.setId(EntityIdCodec.toDomain(request.getId()));
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        if (StringUtils.isNotEmpty(request.getParentId())) {
            entity.setParentId(request.getParentId());
        }
        entity.setName(request.getName());
        entity.setPerms(request.getPerms());
        entity.setRank(AccessRankCodec.toDomain(request.getRanks()));
        entity.setVisibility(
                Boolean.TRUE.equals(request.getDisplay()) ? MenuVisibility.VISIBLE : MenuVisibility.HIDDEN);
        entity.setDisplayParams(request.getDisplayParams());
        entity.setUrl(request.getUrl());
        return entity;
    }
}
