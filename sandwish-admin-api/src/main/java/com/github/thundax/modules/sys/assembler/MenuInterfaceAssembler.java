package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.config.Global;
import com.github.thundax.common.persistence.DataEntity;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.request.MenuSaveRequest;
import com.github.thundax.modules.sys.response.MenuResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class MenuInterfaceAssembler {

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
        response.setId(entity.getId());
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
        entity.setDisplayFlag(Boolean.TRUE.equals(request.getDisplay()) ? Global.SHOW : Global.HIDE);
        entity.setDisplayParams(request.getDisplayParams());
        entity.setUrl(request.getUrl());

        return entity;
    }

    private static MenuResponse baseEntityToResponse(MenuResponse response, DataEntity entity) {
        response.setId(entity.getId());
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        return response;
    }

    private static <T extends DataEntity<T>> T baseRequestToEntity(T entity, MenuSaveRequest request) {
        entity.setId(request.getId());
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        return entity;
    }
}
