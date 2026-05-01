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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class UserInterfaceAssembler {
    private UserInterfaceAssembler() {}

    public static EntityId toEntityId(String id) {
        return EntityIdCodec.toDomain(id);
    }

    @NonNull
    public static UserResponse toResponse(
            User entity, Office office, List<Role> roleList, Function<EntityId, Office> officeLoader) {
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
                UserApiController.getAvatarUrl(EntityIdCodec.toValue(entity.getId()), UserAccessHolder.currentToken()));
        response.setSuperAdmin(entity.isSuper());
        response.setAdmin(entity.isAdmin());
        response.setEnable(entity.isEnable());
        response.setRegisterDate(entity.getRegisterDate());
        response.setRegisterIp(entity.getRegisterIp());
        response.setLastLoginDate(entity.getLastLoginDate());
        response.setLastLoginIp(entity.getLastLoginIp());
        response.setOffice(toOfficeResponse(office, officeLoader));
        response.setRoleList(
                roleList == null
                        ? new ArrayList<>()
                        : roleList.stream()
                                .map(UserInterfaceAssembler::toRoleResponse)
                                .collect(Collectors.toList()));
        return response;
    }

    @NonNull
    public static UserOfficeResponse toOfficeResponse(Office entity, Function<EntityId, Office> officeLoader) {
        if (entity == null) {
            return new UserOfficeResponse();
        }

        UserOfficeResponse response = new UserOfficeResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        if (StringUtils.isNotBlank(entity.getParentId())) {
            response.setParentId(entity.getParentId());
        }
        response.setName(entity.getName());
        response.setNamePath(namePath(entity, officeLoader));
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
    public static User toEntity(@NonNull User entity, @NonNull UserSaveRequest request) {
        entity.setId(EntityIdCodec.toDomain(request.getId()));
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
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
