package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.controller.request.RoleQueryRequest;
import com.github.thundax.modules.sys.controller.request.RoleSaveRequest;
import com.github.thundax.modules.sys.controller.response.RoleDepartmentResponse;
import com.github.thundax.modules.sys.controller.response.RoleMenuResponse;
import com.github.thundax.modules.sys.controller.response.RoleResponse;
import com.github.thundax.modules.sys.controller.response.RoleUserResponse;
import com.github.thundax.modules.sys.controller.response.RoleUserTreeNodeResponse;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.RolePrivilege;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class RoleInterfaceAssembler {
    private RoleInterfaceAssembler() {}

    @NonNull
    public static RoleResponse toResponse(Role entity, List<Menu> menuList) {
        if (entity == null) {
            return new RoleResponse();
        }

        RoleResponse response = new RoleResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        response.setName(entity.getName());
        response.setAdmin(entity.isAdmin());
        response.setEnable(entity.isEnable());
        response.setMenuList(
                menuList == null
                        ? new ArrayList<>()
                        : menuList.stream()
                                .map(RoleInterfaceAssembler::toMenuResponse)
                                .collect(Collectors.toList()));
        return response;
    }

    @NonNull
    public static RoleMenuResponse toMenuResponse(Menu entity) {
        if (entity == null) {
            return new RoleMenuResponse();
        }

        RoleMenuResponse response = new RoleMenuResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        String parentId = EntityIdCodec.toValue(entity.getParentId());
        if (StringUtils.isNotBlank(parentId)) {
            response.setParentId(parentId);
        }
        response.setName(entity.getName());
        response.setPerms(entity.getPerms());
        return response;
    }

    @NonNull
    public static RoleUserResponse toUserResponse(
            User entity, String loginName, Department department, Function<EntityId, Department> departmentLoader) {
        if (entity == null) {
            return new RoleUserResponse();
        }

        RoleUserResponse response = new RoleUserResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setName(entity.getName());
        response.setLoginName(loginName);
        response.setDepartment(toDepartmentResponse(department, departmentLoader));
        return response;
    }

    @NonNull
    public static RoleUserTreeNodeResponse toDepartmentTreeNode(String id, Department entity) {
        RoleUserTreeNodeResponse response = new RoleUserTreeNodeResponse();
        response.setId(id);
        if (StringUtils.isNotBlank(entity.getParentId())) {
            response.setParentId(idPrefix(entity.getParentId()));
        }
        response.setName(entity.getName());
        return response;
    }

    @NonNull
    public static RoleUserTreeNodeResponse toUserTreeNode(
            String departmentIdPrefix,
            User entity,
            String loginName,
            Department department,
            Function<EntityId, Department> departmentLoader) {
        RoleUserTreeNodeResponse response = new RoleUserTreeNodeResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setParentId(departmentIdPrefix + entity.getDepartmentId());
        response.setName(entity.getName());
        response.setUser(toUserResponse(entity, loginName, department, departmentLoader));
        return response;
    }

    @NonNull
    public static RoleQuery toQuery(@NonNull RoleQueryRequest request) {
        RoleQuery query = new RoleQuery();
        if (request.getEnable() != null) {
            query.setStatus(request.getEnable() ? RoleStatus.ENABLED : RoleStatus.DISABLED);
        }
        return query;
    }

    @NonNull
    public static Role toEntity(@NonNull Role entity, @NonNull RoleSaveRequest request) {
        entity.setId(EntityIdCodec.toDomain(request.getId()));
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        entity.setName(request.getName());
        entity.setPrivilege(Boolean.TRUE.equals(request.getAdmin()) ? RolePrivilege.ADMIN : RolePrivilege.NORMAL);
        entity.setStatus(Boolean.TRUE.equals(request.getEnable()) ? RoleStatus.ENABLED : RoleStatus.DISABLED);
        entity.setMenuIdList(
                request.getMenuList() == null
                        ? new ArrayList<>()
                        : request.getMenuList().stream()
                                .map(menu -> menu.getId())
                                .collect(Collectors.toList()));
        return entity;
    }

    @NonNull
    private static RoleDepartmentResponse toDepartmentResponse(
            Department entity, Function<EntityId, Department> departmentLoader) {
        if (entity == null) {
            return new RoleDepartmentResponse();
        }

        RoleDepartmentResponse response = new RoleDepartmentResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setName(entity.getName());
        response.setNamePath(namePath(entity, departmentLoader));
        return response;
    }

    private static String idPrefix(String id) {
        return "DEPARTMENT_" + id;
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
