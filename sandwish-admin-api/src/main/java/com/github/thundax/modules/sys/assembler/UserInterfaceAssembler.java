package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.config.Global;
import com.github.thundax.common.persistence.DataEntity;
import org.apache.commons.lang3.StringUtils;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.controller.UserApiController;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.request.UserSaveRequest;
import com.github.thundax.modules.sys.response.UserOfficeResponse;
import com.github.thundax.modules.sys.response.UserResponse;
import com.github.thundax.modules.sys.response.UserRoleResponse;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class UserInterfaceAssembler {

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
                UserApiController.getAvatarUrl(entity.getId(), UserAccessHolder.currentToken()));

        response.setSuperAdmin(entity.isSuper());
        response.setAdmin(entity.isAdmin());
        response.setEnable(entity.isEnable());

        response.setRegisterDate(entity.getRegisterDate());
        response.setRegisterIp(entity.getRegisterIp());
        response.setLastLoginDate(entity.getLastLoginDate());
        response.setLastLoginIp(entity.getLastLoginIp());

        response.setOffice(toOfficeResponse(entity.getOffice()));
        response.setRoleList(
                entity.getRoleList() == null
                        ? new ArrayList<>()
                        : entity.getRoleList().stream()
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
        response.setId(entity.getId());
        if (StringUtils.isNotBlank(entity.getParentId())) {
            response.setParentId(entity.getParentId());
        }
        response.setName(entity.getName());
        response.setNamePath(entity.getNamePath());

        return response;
    }

    @NonNull
    public UserRoleResponse toRoleResponse(Role entity) {
        if (entity == null) {
            return new UserRoleResponse();
        }

        UserRoleResponse response = new UserRoleResponse();
        response.setId(entity.getId());
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

        entity.setAdminFlag(Boolean.TRUE.equals(request.getAdmin()) ? Global.YES : Global.NO);
        entity.setEnableFlag(
                Boolean.TRUE.equals(request.getEnable()) ? Global.ENABLE : Global.DISABLE);

        entity.setRoleIdList(
                request.getRoleList() == null
                        ? new ArrayList<>()
                        : request.getRoleList().stream()
                                .map(role -> role.getId())
                                .collect(Collectors.toList()));

        return entity;
    }

    private static UserResponse baseEntityToResponse(UserResponse response, DataEntity entity) {
        response.setId(entity.getId());
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        return response;
    }

    private static <T extends DataEntity<T>> T baseRequestToEntity(
            T entity, UserSaveRequest request) {
        entity.setId(request.getId());
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity.setRemarks(request.getRemarks());
        return entity;
    }
}
