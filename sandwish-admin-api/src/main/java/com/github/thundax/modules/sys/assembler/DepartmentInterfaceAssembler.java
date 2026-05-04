package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.controller.request.DepartmentQueryRequest;
import com.github.thundax.modules.sys.controller.request.DepartmentSaveRequest;
import com.github.thundax.modules.sys.controller.response.DepartmentResponse;
import com.github.thundax.modules.sys.entity.Department;
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
            return new DepartmentResponse();
        }

        DepartmentResponse response = new DepartmentResponse();
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
        response.setNamePath(namePath(entity, departmentLoader));
        return response;
    }

    @NonNull
    public static DepartmentResponse toTreeResponse(Department entity) {
        if (entity == null) {
            return new DepartmentResponse();
        }

        DepartmentResponse response = new DepartmentResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        if (StringUtils.isNotBlank(entity.getParentId())) {
            response.setParentId(entity.getParentId());
        }
        response.setName(entity.getName());
        response.setShortName(entity.getShortName());
        return response;
    }

    @NonNull
    public static DepartmentQuery toQuery(@NonNull DepartmentQueryRequest request) {
        DepartmentQuery query = new DepartmentQuery();
        query.setParentId(request.getParentId());
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
        if (StringUtils.isNotEmpty(request.getParentId())) {
            entity.setParentId(request.getParentId());
        }
        entity.setName(request.getName());
        entity.setShortName(request.getShortName());
        return entity;
    }

    private static String namePath(Department department, Function<EntityId, Department> departmentLoader) {
        List<String> names = new ArrayList<>();
        Department node = department;
        while (node != null && EntityIdCodec.toValue(node.getId()) != null) {
            node = departmentLoader.apply(node.getId());
            if (node != null) {
                names.add(0, node.getName());
                node = departmentLoader.apply(EntityIdCodec.toDomain(node.getParentId()));
            }
        }
        return StringUtils.join(names, "/");
    }
}
