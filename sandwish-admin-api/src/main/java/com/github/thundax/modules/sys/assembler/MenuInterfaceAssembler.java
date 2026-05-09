package com.github.thundax.modules.sys.assembler;

import com.github.thundax.modules.sys.codec.AccessRankCodec;
import com.github.thundax.modules.sys.controller.request.MenuQueryRequest;
import com.github.thundax.modules.sys.controller.request.MenuSaveRequest;
import com.github.thundax.modules.sys.controller.response.MenuResponse;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.valueobject.MenuIdCodec;
import com.github.thundax.modules.sys.service.command.ChangeMenuInfoCommand;
import com.github.thundax.modules.sys.service.command.CreateMenuCommand;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import org.springframework.lang.NonNull;

public final class MenuInterfaceAssembler {
    private MenuInterfaceAssembler() {}

    @NonNull
    public static MenuResponse toResponse(Menu entity) {
        if (entity == null) {
            return MenuResponse.builder().build();
        }
        Long parentId = MenuIdCodec.toValue(entity.getParentId());
        return MenuResponse.builder()
                .id(MenuIdCodec.toValue(entity.getId()))
                .remarks(entity.getRemarks())
                .priority(entity.getPriority())
                .parentId(parentId)
                .name(entity.getName())
                .perms(entity.getPerms())
                .ranks(AccessRankCodec.toValue(entity.getRank()))
                .display(entity.isDisplay())
                .displayParams(entity.getDisplayParams())
                .url(entity.getUrl())
                .build();
    }

    @NonNull
    public static MenuResponse toTreeResponse(Menu entity) {
        if (entity == null) {
            return MenuResponse.builder().build();
        }
        return MenuResponse.builder()
                .id(MenuIdCodec.toValue(entity.getId()))
                .parentId(MenuIdCodec.toValue(entity.getParentId()))
                .name(entity.getName())
                .build();
    }

    @NonNull
    public static MenuQuery toQuery(@NonNull MenuQueryRequest request) {
        MenuQuery query = new MenuQuery();
        query.setParentId(MenuIdCodec.toDomain(request.getParentId()));
        if (request.getDisplay() != null) {
            query.setVisibility(request.getDisplay() ? MenuVisibility.VISIBLE : MenuVisibility.HIDDEN);
        }
        return query;
    }

    @NonNull
    public static Menu toEntity(@NonNull Menu entity, @NonNull MenuSaveRequest request) {
        entity.setId(MenuIdCodec.toDomain(request.getId()));
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        if (request.getParentId() != null) {
            entity.setParentId(MenuIdCodec.toDomain(request.getParentId()));
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

    @NonNull
    public static CreateMenuCommand toCreateCommand(@NonNull MenuSaveRequest request) {
        Menu entity = toEntity(new Menu(), request);
        return new CreateMenuCommand(
                entity.getId(),
                entity.getParentId(),
                entity.getName(),
                entity.getPerms(),
                entity.getRank(),
                entity.getVisibility(),
                entity.getDisplayParams(),
                entity.getUrl(),
                entity.getTarget(),
                entity.getPriority(),
                entity.getRemarks());
    }

    @NonNull
    public static ChangeMenuInfoCommand toChangeInfoCommand(@NonNull MenuSaveRequest request) {
        Menu entity = toEntity(new Menu(), request);
        return new ChangeMenuInfoCommand(
                entity.getId(),
                entity.getParentId(),
                entity.getName(),
                entity.getPerms(),
                entity.getRank(),
                entity.getVisibility(),
                entity.getDisplayParams(),
                entity.getUrl(),
                entity.getTarget(),
                entity.getPriority(),
                entity.getRemarks());
    }
}
