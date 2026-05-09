package com.github.thundax.modules.sys.assembler;

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
import com.github.thundax.modules.sys.entity.valueobject.DepartmentId;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.MenuId;
import com.github.thundax.modules.sys.entity.valueobject.MenuIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.RoleIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.command.ChangeRoleInfoCommand;
import com.github.thundax.modules.sys.service.command.CreateRoleCommand;
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
            return RoleResponse.builder().build();
        }

        return RoleResponse.builder()
                .id(RoleIdCodec.toValue(entity.getId()))
                .remarks(entity.getRemarks())
                .priority(entity.getPriority())
                .name(entity.getName())
                .admin(entity.isAdmin())
                .enable(entity.isEnable())
                .menuList(
                        menuList == null
                                ? new ArrayList<>()
                                : menuList.stream()
                                        .map(RoleInterfaceAssembler::toMenuResponse)
                                        .collect(Collectors.toList()))
                .build();
    }

    @NonNull
    public static RoleMenuResponse toMenuResponse(Menu entity) {
        if (entity == null) {
            return RoleMenuResponse.builder().build();
        }

        Long parentId = MenuIdCodec.toValue(entity.getParentId());
        return RoleMenuResponse.builder()
                .id(MenuIdCodec.toValue(entity.getId()))
                .parentId(parentId)
                .name(entity.getName())
                .perms(entity.getPerms())
                .build();
    }

    @NonNull
    public static RoleUserResponse toUserResponse(
            User entity, String loginName, Department department, Function<DepartmentId, Department> departmentLoader) {
        if (entity == null) {
            return RoleUserResponse.builder().build();
        }

        return RoleUserResponse.builder()
                .id(UserIdCodec.toValue(entity.getId()))
                .name(entity.getName())
                .loginName(loginName)
                .department(toDepartmentResponse(department, departmentLoader))
                .build();
    }

    @NonNull
    public static RoleUserTreeNodeResponse toDepartmentTreeNode(String id, Department entity) {
        return RoleUserTreeNodeResponse.builder()
                .id(id)
                .parentId(
                        entity.getParentId() == null ? null : idPrefix(DepartmentIdCodec.toValue(entity.getParentId())))
                .name(entity.getName())
                .build();
    }

    @NonNull
    public static RoleUserTreeNodeResponse toUserTreeNode(
            String departmentIdPrefix,
            User entity,
            String loginName,
            Department department,
            Function<DepartmentId, Department> departmentLoader) {
        return RoleUserTreeNodeResponse.builder()
                .id(String.valueOf(UserIdCodec.toValue(entity.getId())))
                .parentId(departmentIdPrefix + DepartmentIdCodec.toValue(entity.getDepartmentId()))
                .name(entity.getName())
                .user(toUserResponse(entity, loginName, department, departmentLoader))
                .build();
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
    public static CreateRoleCommand toCreateCommand(@NonNull RoleSaveRequest request) {
        CreateRoleCommand command = new CreateRoleCommand();
        command.setId(RoleIdCodec.toDomain(request.getId()));
        if (request.getPriority() != null) {
            command.setPriority(request.getPriority());
        }
        command.setRemarks(request.getRemarks());
        command.setName(request.getName());
        command.setPrivilege(Boolean.TRUE.equals(request.getAdmin()) ? RolePrivilege.ADMIN : RolePrivilege.NORMAL);
        command.setStatus(Boolean.TRUE.equals(request.getEnable()) ? RoleStatus.ENABLED : RoleStatus.DISABLED);
        command.setMenuIdList(toMenuIds(request));
        return command;
    }

    @NonNull
    public static ChangeRoleInfoCommand toChangeInfoCommand(@NonNull RoleSaveRequest request) {
        ChangeRoleInfoCommand command = new ChangeRoleInfoCommand();
        command.setId(RoleIdCodec.toDomain(request.getId()));
        if (request.getPriority() != null) {
            command.setPriority(request.getPriority());
        }
        command.setRemarks(request.getRemarks());
        command.setName(request.getName());
        command.setPrivilege(Boolean.TRUE.equals(request.getAdmin()) ? RolePrivilege.ADMIN : RolePrivilege.NORMAL);
        command.setStatus(Boolean.TRUE.equals(request.getEnable()) ? RoleStatus.ENABLED : RoleStatus.DISABLED);
        command.setMenuIdList(toMenuIds(request));
        return command;
    }

    @NonNull
    public static Role toEntity(@NonNull Role entity, @NonNull RoleSaveRequest request) {
        entity.setId(RoleIdCodec.toDomain(request.getId()));
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

    private static List<MenuId> toMenuIds(RoleSaveRequest request) {
        return request.getMenuList() == null
                ? new ArrayList<>()
                : request.getMenuList().stream()
                        .map(menu -> MenuIdCodec.toDomain(menu.getId()))
                        .collect(Collectors.toList());
    }

    @NonNull
    private static RoleDepartmentResponse toDepartmentResponse(
            Department entity, Function<DepartmentId, Department> departmentLoader) {
        if (entity == null) {
            return RoleDepartmentResponse.builder().build();
        }

        return RoleDepartmentResponse.builder()
                .id(DepartmentIdCodec.toValue(entity.getId()))
                .name(entity.getName())
                .namePath(namePath(entity, departmentLoader))
                .build();
    }

    private static String idPrefix(Long id) {
        return "DEPARTMENT_" + id;
    }

    private static String namePath(Department department, Function<DepartmentId, Department> departmentLoader) {
        List<String> names = new ArrayList<>();
        Department node = department;
        while (node != null && DepartmentIdCodec.toValue(node.getId()) != null) {
            node = departmentLoader.apply(node.getId());
            if (node != null) {
                names.add(0, node.getName());
                node = departmentLoader.apply(node.getParentId());
            }
        }
        return StringUtils.join(names, "/");
    }
}
