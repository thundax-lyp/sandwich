package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.controller.request.LogPageRequest;
import com.github.thundax.modules.sys.controller.response.LogDepartmentResponse;
import com.github.thundax.modules.sys.controller.response.LogResponse;
import com.github.thundax.modules.sys.controller.response.LogUserResponse;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.query.LogQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class LogInterfaceAssembler {
    private LogInterfaceAssembler() {}

    @NonNull
    public static LogResponse toResponse(
            Log entity,
            User user,
            String loginName,
            Department department,
            Function<EntityId, Department> departmentLoader) {
        if (entity == null) {
            return new LogResponse();
        }

        LogResponse response = new LogResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setType(entity.getType() == null ? null : entity.getType().value());
        response.setTitle(entity.getTitle());
        response.setRemoteAddr(entity.getRemoteAddr());
        response.setUserAgent(entity.getUserAgent());
        response.setMethod(entity.getMethod());
        response.setRequestUri(entity.getRequestUri());
        response.setRequestParams(entity.getRequestParams());
        response.setCreateDate(entity.getLogDate());
        response.setCreateUser(toUserResponse(user, loginName, department, departmentLoader));
        return response;
    }

    @NonNull
    public static LogQuery toQuery(@NonNull LogPageRequest request) {
        LogQuery query = new LogQuery();
        query.setTitle(request.getTitle());
        query.setRemoteAddr(request.getRemoteAddr());
        query.setRequestUri(request.getRequestUri());
        query.setUserLoginName(request.getUserLoginName());
        query.setUserName(request.getUserName());
        query.setBeginDate(request.getBeginDate());
        query.setEndDate(request.getEndDate());
        return query;
    }

    @NonNull
    private static LogUserResponse toUserResponse(
            User entity, String loginName, Department department, Function<EntityId, Department> departmentLoader) {
        if (entity == null) {
            return new LogUserResponse();
        }

        LogUserResponse response = new LogUserResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setLoginName(loginName);
        response.setName(entity.getName());
        response.setDepartment(toDepartmentResponse(department, departmentLoader));
        return response;
    }

    @NonNull
    private static LogDepartmentResponse toDepartmentResponse(
            Department entity, Function<EntityId, Department> departmentLoader) {
        if (entity == null) {
            return new LogDepartmentResponse();
        }

        LogDepartmentResponse response = new LogDepartmentResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setName(entity.getName());
        response.setNamePath(namePath(entity, departmentLoader));
        return response;
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
