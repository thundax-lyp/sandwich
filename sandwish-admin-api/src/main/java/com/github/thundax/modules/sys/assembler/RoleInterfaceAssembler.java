package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.config.Global;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.DataEntity;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.request.RoleSaveRequest;
import com.github.thundax.modules.sys.response.RoleMenuResponse;
import com.github.thundax.modules.sys.response.RoleOfficeResponse;
import com.github.thundax.modules.sys.response.RoleResponse;
import com.github.thundax.modules.sys.response.RoleUserResponse;
import com.github.thundax.modules.sys.response.RoleUserTreeNodeResponse;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RoleInterfaceAssembler {

    public EntityId toEntityId(String id) {
        return EntityIdCodec.toDomain(id);
    }

    @NonNull
    public RoleResponse toResponse(Role entity) {
        if (entity == null) {
            return new RoleResponse();
        }

        RoleResponse response = baseEntityToResponse(new RoleResponse(), entity);

        response.setName(entity.getName());
        response.setAdmin(entity.isAdmin());
        response.setEnable(entity.isEnable());
        response.setMenuList(
                entity.getMenuList() == null
                        ? new ArrayList<>()
                        : entity.getMenuList().stream()
                                .map(menu -> this.toMenuResponse(menu))
                                .collect(Collectors.toList()));

        return response;
    }

    @NonNull
    public RoleMenuResponse toMenuResponse(Menu entity) {
        if (entity == null) {
            return new RoleMenuResponse();
        }

        RoleMenuResponse response = new RoleMenuResponse();
        response.setId(entity.getId());
        if (StringUtils.isNotBlank(entity.getParentId())) {
            response.setParentId(entity.getParentId());
        }
        response.setName(entity.getName());
        response.setPerms(entity.getPerms());
        return response;
    }

    @NonNull
    public RoleOfficeResponse toOfficeResponse(Office entity) {
        if (entity == null) {
            return new RoleOfficeResponse();
        }

        RoleOfficeResponse response = new RoleOfficeResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setNamePath(entity.getNamePath());
        return response;
    }

    @NonNull
    public RoleUserResponse toUserResponse(User entity) {
        if (entity == null) {
            return new RoleUserResponse();
        }

        RoleUserResponse response = new RoleUserResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setLoginName(entity.getLoginName());
        response.setOffice(toOfficeResponse(entity.getOffice()));
        return response;
    }

    @NonNull
    public RoleUserTreeNodeResponse toOfficeTreeNode(String id, Office entity) {
        RoleUserTreeNodeResponse response = new RoleUserTreeNodeResponse();
        response.setId(id);
        if (StringUtils.isNotBlank(entity.getParentId())) {
            response.setParentId(idPrefix(entity.getParentId()));
        }
        response.setName(entity.getName());
        return response;
    }

    @NonNull
    public RoleUserTreeNodeResponse toUserTreeNode(String officeIdPrefix, User entity) {
        RoleUserTreeNodeResponse response = new RoleUserTreeNodeResponse();
        response.setId(entity.getId());
        response.setParentId(officeIdPrefix + entity.getOfficeId());
        response.setName(entity.getName());
        response.setUser(toUserResponse(entity));
        return response;
    }

    @NonNull
    public Role toEntity(@NonNull Role entity, @NonNull RoleSaveRequest request) {
        baseRequestToEntity(entity, request);

        entity.setName(request.getName());
        entity.setAdminFlag(Boolean.TRUE.equals(request.getAdmin()) ? Global.YES : Global.NO);
        entity.setEnableFlag(Boolean.TRUE.equals(request.getEnable()) ? Global.ENABLE : Global.DISABLE);
        entity.setMenuIdList(
                request.getMenuList() == null
                        ? new ArrayList<>()
                        : request.getMenuList().stream()
                                .map(menu -> menu.getId())
                                .collect(Collectors.toList()));

        return entity;
    }

    private String idPrefix(String id) {
        return "OFFICE_" + id;
    }

    private static RoleResponse baseEntityToResponse(RoleResponse response, DataEntity entity) {
        response.setId(entity.getId());
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        return response;
    }

    private static <T extends DataEntity<T>> T baseRequestToEntity(T entity, RoleSaveRequest request) {
        entity.setId(request.getId());
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        return entity;
    }
}
