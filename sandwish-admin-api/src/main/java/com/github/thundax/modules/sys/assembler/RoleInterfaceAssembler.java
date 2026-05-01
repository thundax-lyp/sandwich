package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.RolePrivilege;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.request.RoleSaveRequest;
import com.github.thundax.modules.sys.response.RoleMenuResponse;
import com.github.thundax.modules.sys.response.RoleOfficeResponse;
import com.github.thundax.modules.sys.response.RoleResponse;
import com.github.thundax.modules.sys.response.RoleUserResponse;
import com.github.thundax.modules.sys.response.RoleUserTreeNodeResponse;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.OfficeService;
import com.github.thundax.modules.sys.service.RoleService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RoleInterfaceAssembler {

    private final RoleService roleService;
    private final MenuService menuService;
    private final OfficeService officeService;

    public RoleInterfaceAssembler(RoleService roleService, MenuService menuService, OfficeService officeService) {
        this.roleService = roleService;
        this.menuService = menuService;
        this.officeService = officeService;
    }

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
        List<Menu> menuList = roleService.findRoleMenu(entity);
        response.setMenuList(
                menuList == null
                        ? new ArrayList<>()
                        : menuList.stream()
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
        response.setId(EntityIdCodec.toValue(entity.getId()));
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
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setName(entity.getName());
        response.setNamePath(namePath(entity));
        return response;
    }

    @NonNull
    public RoleUserResponse toUserResponse(User entity) {
        if (entity == null) {
            return new RoleUserResponse();
        }

        RoleUserResponse response = new RoleUserResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setName(entity.getName());
        response.setLoginName(entity.getLoginName());
        response.setOffice(toOfficeResponse(officeService.get(EntityIdCodec.toDomain(entity.getOfficeId()))));
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
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setParentId(officeIdPrefix + entity.getOfficeId());
        response.setName(entity.getName());
        response.setUser(toUserResponse(entity));
        return response;
    }

    @NonNull
    public Role toEntity(@NonNull Role entity, @NonNull RoleSaveRequest request) {
        baseRequestToEntity(entity, request);

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

    private String idPrefix(String id) {
        return "OFFICE_" + id;
    }

    private static RoleResponse baseEntityToResponse(RoleResponse response, Role entity) {
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        return response;
    }

    private static Role baseRequestToEntity(Role entity, RoleSaveRequest request) {
        entity.setId(EntityIdCodec.toDomain(request.getId()));
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        return entity;
    }

    private String namePath(Office office) {
        List<String> names = new ArrayList<>();
        Office node = office;
        while (node != null && EntityIdCodec.toValue(node.getId()) != null) {
            node = officeService.get(node.getId());
            if (node != null) {
                names.add(0, node.getName());
                node = officeService.get(EntityIdCodec.toDomain(node.getParentId()));
            }
        }
        return StringUtils.join(names, "/");
    }
}
