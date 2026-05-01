package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.controller.UserApiController;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.request.UserSaveRequest;
import com.github.thundax.modules.sys.response.UserOfficeResponse;
import com.github.thundax.modules.sys.response.UserResponse;
import com.github.thundax.modules.sys.response.UserRoleResponse;
import com.github.thundax.modules.sys.service.OfficeService;
import com.github.thundax.modules.sys.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class UserInterfaceAssembler {

    private final OfficeService officeService;
    private final UserService userService;

    public UserInterfaceAssembler(OfficeService officeService, UserService userService) {
        this.officeService = officeService;
        this.userService = userService;
    }

    public EntityId toEntityId(String id) {
        return EntityIdCodec.toDomain(id);
    }

    @NonNull
    public UserResponse toResponse(User entity) {
        if (entity == null) {
            return new UserResponse();
        }

        UserResponse response = baseEntityToResponse(new UserResponse(), entity);

        response.setLoginName(entity.getLoginName());
        response.setRanks(entity.getRanks());

        response.setName(entity.getName());
        response.setEmail(entity.getEmail());
        response.setMobile(entity.getMobile());
        response.setAvatar(
                UserApiController.getAvatarUrl(EntityIdCodec.toValue(entity.getId()), UserAccessHolder.currentToken()));

        response.setSuperAdmin(entity.isSuper());
        response.setAdmin(entity.isAdmin());
        response.setEnable(entity.isEnable());

        response.setRegisterDate(entity.getRegisterDate());
        response.setRegisterIp(entity.getRegisterIp());
        response.setLastLoginDate(entity.getLastLoginDate());
        response.setLastLoginIp(entity.getLastLoginIp());

        response.setOffice(toOfficeResponse(officeService.get(EntityIdCodec.toDomain(entity.getOfficeId()))));
        List<Role> roleList = userService.findUserRole(entity);
        response.setRoleList(
                roleList == null
                        ? new ArrayList<>()
                        : roleList.stream()
                                .map(role -> this.toRoleResponse(role))
                                .collect(Collectors.toList()));

        return response;
    }

    @NonNull
    public UserOfficeResponse toOfficeResponse(Office entity) {
        if (entity == null) {
            return new UserOfficeResponse();
        }

        UserOfficeResponse response = new UserOfficeResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        if (StringUtils.isNotBlank(entity.getParentId())) {
            response.setParentId(entity.getParentId());
        }
        response.setName(entity.getName());
        response.setNamePath(namePath(entity));

        return response;
    }

    @NonNull
    public UserRoleResponse toRoleResponse(Role entity) {
        if (entity == null) {
            return new UserRoleResponse();
        }

        UserRoleResponse response = new UserRoleResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setName(entity.getName());

        return response;
    }

    @NonNull
    public User toEntity(@NonNull User entity, @NonNull UserSaveRequest request) {
        baseRequestToEntity(entity, request);

        if (request.getOffice() != null) {
            entity.setOfficeId(request.getOffice().getId());
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

    private static UserResponse baseEntityToResponse(UserResponse response, User entity) {
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        return response;
    }

    private static User baseRequestToEntity(User entity, UserSaveRequest request) {
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
