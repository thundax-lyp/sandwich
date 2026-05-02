package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.controller.request.RoleQueryRequest;
import com.github.thundax.modules.sys.controller.request.RoleSaveRequest;
import com.github.thundax.modules.sys.controller.response.RoleMenuResponse;
import com.github.thundax.modules.sys.controller.response.RoleOfficeResponse;
import com.github.thundax.modules.sys.controller.response.RoleResponse;
import com.github.thundax.modules.sys.controller.response.RoleUserResponse;
import com.github.thundax.modules.sys.controller.response.RoleUserTreeNodeResponse;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Office;
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
        if (StringUtils.isNotBlank(entity.getParentId())) {
            response.setParentId(entity.getParentId());
        }
        response.setName(entity.getName());
        response.setPerms(entity.getPerms());
        return response;
    }

    @NonNull
    public static RoleUserResponse toUserResponse(User entity, Office office, Function<EntityId, Office> officeLoader) {
        if (entity == null) {
            return new RoleUserResponse();
        }

        RoleUserResponse response = new RoleUserResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setName(entity.getName());
        response.setLoginName(entity.getLoginName());
        response.setOffice(toOfficeResponse(office, officeLoader));
        return response;
    }

    @NonNull
    public static RoleUserTreeNodeResponse toOfficeTreeNode(String id, Office entity) {
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
            String officeIdPrefix, User entity, Office office, Function<EntityId, Office> officeLoader) {
        RoleUserTreeNodeResponse response = new RoleUserTreeNodeResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setParentId(officeIdPrefix + entity.getOfficeId());
        response.setName(entity.getName());
        response.setUser(toUserResponse(entity, office, officeLoader));
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
    private static RoleOfficeResponse toOfficeResponse(Office entity, Function<EntityId, Office> officeLoader) {
        if (entity == null) {
            return new RoleOfficeResponse();
        }

        RoleOfficeResponse response = new RoleOfficeResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setName(entity.getName());
        response.setNamePath(namePath(entity, officeLoader));
        return response;
    }

    private static String idPrefix(String id) {
        return "OFFICE_" + id;
    }

    private static String namePath(Office office, Function<EntityId, Office> officeLoader) {
        List<String> names = new ArrayList<>();
        Office node = office;
        while (node != null && EntityIdCodec.toValue(node.getId()) != null) {
            node = officeLoader.apply(node.getId());
            if (node != null) {
                names.add(0, node.getName());
                node = officeLoader.apply(EntityIdCodec.toDomain(node.getParentId()));
            }
        }
        return StringUtils.join(names, "/");
    }
}
