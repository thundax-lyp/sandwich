package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.controller.request.DepartmentQueryRequest;
import com.github.thundax.modules.sys.controller.request.DepartmentSaveRequest;
import com.github.thundax.modules.sys.controller.response.DepartmentResponse;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.service.command.ChangeDepartmentInfoCommand;
import com.github.thundax.modules.sys.service.command.CreateDepartmentCommand;
import com.github.thundax.modules.sys.service.query.DepartmentQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class DepartmentInterfaceAssembler {
    private DepartmentInterfaceAssembler() {}

    @NonNull
    public static DepartmentResponse toResponse(Department entity, Function<EntityId, Department> departmentLoader) {
        if (entity == null) {
            return DepartmentResponse.builder().build();
        }
        return DepartmentResponse.builder()
                .id(EntityIdCodec.toValue(entity.getId()))
                .remarks(entity.getRemarks())
                .priority(entity.getPriority())
                .parentId(EntityIdCodec.toValue(entity.getParentId()))
                .name(entity.getName())
                .shortName(entity.getShortName())
                .namePath(namePath(entity, departmentLoader))
                .build();
    }

    @NonNull
    public static DepartmentResponse toTreeResponse(Department entity) {
        if (entity == null) {
            return DepartmentResponse.builder().build();
        }
        return DepartmentResponse.builder()
                .id(EntityIdCodec.toValue(entity.getId()))
                .parentId(EntityIdCodec.toValue(entity.getParentId()))
                .name(entity.getName())
                .shortName(entity.getShortName())
                .build();
    }

    @NonNull
    public static DepartmentQuery toQuery(@NonNull DepartmentQueryRequest request) {
        DepartmentQuery query = new DepartmentQuery();
        query.setParentId(EntityIdCodec.toDomain(request.getParentId()));
        query.setName(request.getName());
        query.setRemarks(request.getRemarks());
        return query;
    }

    @NonNull
    public static Department toEntity(@NonNull Department entity, @NonNull DepartmentSaveRequest request) {
        entity.setId(EntityIdCodec.toDomain(request.getId()));
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        if (request.getParentId() != null) {
            entity.setParentId(EntityIdCodec.toDomain(request.getParentId()));
        }
        entity.setName(request.getName());
        entity.setShortName(request.getShortName());
        return entity;
    }

    @NonNull
    public static CreateDepartmentCommand toCreateCommand(@NonNull DepartmentSaveRequest request) {
        Department entity = toEntity(new Department(), request);
        return new CreateDepartmentCommand(
                entity.getId(),
                entity.getParentId(),
                entity.getName(),
                entity.getShortName(),
                entity.getPriority(),
                entity.getRemarks());
    }

    @NonNull
    public static ChangeDepartmentInfoCommand toChangeInfoCommand(@NonNull DepartmentSaveRequest request) {
        Department entity = toEntity(new Department(), request);
        return new ChangeDepartmentInfoCommand(
                entity.getId(),
                entity.getParentId(),
                entity.getName(),
                entity.getShortName(),
                entity.getPriority(),
                entity.getRemarks());
    }

    private static String namePath(Department department, Function<EntityId, Department> departmentLoader) {
        List<String> names = new ArrayList<>();
        Department node = department;
        while (node != null && EntityIdCodec.toValue(node.getId()) != null) {
            node = departmentLoader.apply(node.getId());
            if (node != null) {
                names.add(0, node.getName());
                node = departmentLoader.apply(node.getParentId());
            }
        }
        return StringUtils.join(names, "/");
    }
}
