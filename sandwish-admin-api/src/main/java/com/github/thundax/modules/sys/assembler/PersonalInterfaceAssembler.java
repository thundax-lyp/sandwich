package com.github.thundax.modules.sys.assembler;

import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.controller.UserApiController;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.request.PersonalInfoUpdateRequest;
import com.github.thundax.modules.sys.response.PersonalAvatarResponse;
import com.github.thundax.modules.sys.response.PersonalInfoResponse;
import com.github.thundax.modules.sys.response.PersonalMenuResponse;
import com.github.thundax.modules.sys.response.PersonalPermsResponse;
import com.github.thundax.modules.utils.AvatarUtils;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class PersonalInterfaceAssembler {

    @NonNull
    public PersonalInfoResponse toInfoResponse(User entity) {
        if (entity == null) {
            return new PersonalInfoResponse();
        }

        PersonalInfoResponse response = new PersonalInfoResponse();
        response.setId(entity.getId());
        response.setLoginName(entity.getLoginName());
        response.setRanks(entity.getRanks());
        response.setName(entity.getName());
        response.setMobile(entity.getMobile());
        response.setEmail(entity.getEmail());
        response.setAvatar(readAvatarUrl(entity));
        response.setLastLoginDate(entity.getLastLoginDate());
        response.setLastLoginIp(entity.getLastLoginIp());
        response.setRegisterDate(entity.getRegisterDate());
        response.setRegisterIp(entity.getRegisterIp());
        response.setAdmin(entity.isAdmin());
        response.setSuperAdmin(entity.isSuper());
        return response;
    }

    @NonNull
    public PersonalAvatarResponse toAvatarResponse(User entity) {
        PersonalAvatarResponse response = new PersonalAvatarResponse();
        response.setAvatar(readAvatarUrl(entity));
        return response;
    }

    @NonNull
    public PersonalMenuResponse toMenuResponse(Menu entity) {
        if (entity == null) {
            return new PersonalMenuResponse();
        }

        PersonalMenuResponse response = new PersonalMenuResponse();
        response.setId(entity.getId());
        response.setParentId(entity.getParentId());
        response.setName(entity.getName());
        response.setPriority(entity.getPriority());
        response.setUrl(entity.getUrl());
        response.setDisplayParams(entity.getDisplayParams());
        return response;
    }

    @NonNull
    public PersonalPermsResponse toPermsResponse(Set<String> perms) {
        PersonalPermsResponse response = new PersonalPermsResponse();
        response.setPerms(perms);
        return response;
    }

    @NonNull
    public User toEntity(@NonNull User entity, @NonNull PersonalInfoUpdateRequest request) {
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setMobile(request.getMobile());
        return entity;
    }

    private String readAvatarUrl(User entity) {
        if (entity == null || StringUtils.isBlank(entity.getId()) || !AvatarUtils.existAvatar(entity.getId())) {
            return null;
        }
        return UserApiController.getAvatarUrl(entity.getId(), UserAccessHolder.currentToken());
    }
}
