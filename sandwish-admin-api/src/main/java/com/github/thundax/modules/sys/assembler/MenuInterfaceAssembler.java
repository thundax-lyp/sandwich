package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.request.MenuSaveRequest;
import com.github.thundax.modules.sys.response.MenuResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public class MenuInterfaceAssembler {
    public EntityId toEntityId(String id) {
        return EntityIdCodec.toDomain(id);
    }

    @NonNull
    public MenuResponse toResponse(Menu entity) {
        if (entity == null) {
            return new MenuResponse();
        }
        MenuResponse response = baseEntityToResponse(new MenuResponse(), entity);
        if (StringUtils.isNotEmpty(entity.getParentId())) {
            response.setParentId(entity.getParentId());
        }
        response.setName(entity.getName());
        response.setPerms(entity.getPerms());
        response.setRanks(entity.getRanks());
        response.setDisplay(entity.isDisplay());
        response.setDisplayParams(entity.getDisplayParams());
        response.setUrl(entity.getUrl());
        return response;
    }

    @NonNull
    public MenuResponse toTreeResponse(Menu entity) {
        if (entity == null) {
            return new MenuResponse();
        }
        MenuResponse response = new MenuResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setParentId(entity.getParentId());
        response.setName(entity.getName());
        return response;
    }

    @NonNull
    public Menu toEntity(@NonNull Menu entity, @NonNull MenuSaveRequest request) {
        baseRequestToEntity(entity, request);
        if (StringUtils.isNotEmpty(request.getParentId())) {
            entity.setParentId(request.getParentId());
        }
        entity.setName(request.getName());
        entity.setPerms(request.getPerms());
        entity.setRanks(request.getRanks());
        entity.setVisibility(
                Boolean.TRUE.equals(request.getDisplay()) ? MenuVisibility.VISIBLE : MenuVisibility.HIDDEN);
        entity.setDisplayParams(request.getDisplayParams());
        entity.setUrl(request.getUrl());
        return entity;
    }

    private static MenuResponse baseEntityToResponse(MenuResponse response, Menu entity) {
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        return response;
    }

    private static Menu baseRequestToEntity(Menu entity, MenuSaveRequest request) {
        entity.setId(EntityIdCodec.toDomain(request.getId()));
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        return entity;
    }
}
