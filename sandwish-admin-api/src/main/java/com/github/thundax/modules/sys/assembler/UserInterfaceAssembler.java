package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.controller.UserController;
import com.github.thundax.modules.sys.controller.request.UserQueryRequest;
import com.github.thundax.modules.sys.controller.request.UserSaveRequest;
import com.github.thundax.modules.sys.controller.response.UserDepartmentResponse;
import com.github.thundax.modules.sys.controller.response.UserResponse;
import com.github.thundax.modules.sys.controller.response.UserRoleResponse;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class UserInterfaceAssembler {
    private UserInterfaceAssembler() {}

    @NonNull
    public static UserResponse toResponse(
            User entity, Department department, List<Role> roleList, Function<EntityId, Department> departmentLoader) {
        if (entity == null) {
            return new UserResponse();
        }

        UserResponse response = new UserResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        response.setLoginName(entity.getLoginName());
        response.setRanks(entity.getRanks());
        response.setName(entity.getName());
        response.setEmail(entity.getEmail());
        response.setMobile(entity.getMobile());
        response.setAvatar(
                UserController.getAvatarUrl(EntityIdCodec.toValue(entity.getId()), UserAccessHolder.currentToken()));
        response.setSuperAdmin(entity.isSuper());
        response.setAdmin(entity.isAdmin());
        response.setEnable(entity.isEnable());
        response.setRegisterDate(entity.getRegisterDate());
        response.setRegisterIp(entity.getRegisterIp());
        response.setLastLoginDate(entity.getLastLoginDate());
        response.setLastLoginIp(entity.getLastLoginIp());
        response.setDepartment(toDepartmentResponse(department, departmentLoader));
        response.setRoleList(
                roleList == null
                        ? new ArrayList<>()
                        : roleList.stream()
                                .map(UserInterfaceAssembler::toRoleResponse)
                                .collect(Collectors.toList()));
        return response;
    }

    @NonNull
    public static UserDepartmentResponse toDepartmentResponse(
            Department entity, Function<EntityId, Department> departmentLoader) {
        if (entity == null) {
            return new UserDepartmentResponse();
        }

        UserDepartmentResponse response = new UserDepartmentResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        if (StringUtils.isNotBlank(entity.getParentId())) {
            response.setParentId(entity.getParentId());
        }
        response.setName(entity.getName());
        response.setNamePath(namePath(entity, departmentLoader));
        return response;
    }

    @NonNull
    public static UserRoleResponse toRoleResponse(Role entity) {
        if (entity == null) {
            return new UserRoleResponse();
        }

        UserRoleResponse response = new UserRoleResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setName(entity.getName());
        return response;
    }

    @NonNull
    public static UserQuery toQuery(@NonNull UserQueryRequest request) {
        UserQuery query = new UserQuery();
        query.setDepartmentId(emptyToNull(request.getDepartmentId()));
        query.setLoginName(emptyToNull(request.getLoginName()));
        query.setName(emptyToNull(request.getName()));
        if (request.getEnable() != null) {
            query.setStatus(request.getEnable() ? UserStatus.ENABLED : UserStatus.DISABLED);
        }
        query.setOrderBy(emptyToNull(request.getOrderBy()));
        return query;
    }

    @NonNull
    public static User toEntity(@NonNull User entity, @NonNull UserSaveRequest request) {
        entity.setId(EntityIdCodec.toDomain(request.getId()));
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        if (request.getDepartment() != null) {
            entity.setDepartmentId(request.getDepartment().getId());
        }
        entity.setLoginName(request.getLoginName());
        entity.setRanks(request.getRanks());
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setMobile(request.getMobile());
        entity.setPrivilege(Boolean.TRUE.equals(request.getAdmin()) ? UserPrivilege.ADMIN : UserPrivilege.NORMAL);
        entity.setStatus(Boolean.TRUE.equals(request.getEnable()) ? UserStatus.ENABLED : UserStatus.DISABLED);
        entity.setRoleIdList(
                request.getRoleList() == null
                        ? new ArrayList<>()
                        : request.getRoleList().stream()
                                .map(role -> role.getId())
                                .collect(Collectors.toList()));
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

    private static String emptyToNull(String value) {
        return StringUtils.isEmpty(value) ? null : value;
    }
}
