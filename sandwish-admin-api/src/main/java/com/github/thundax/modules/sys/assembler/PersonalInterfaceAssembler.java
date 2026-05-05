package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.codec.AccessRankCodec;
import com.github.thundax.modules.sys.controller.UserController;
import com.github.thundax.modules.sys.controller.request.PersonalInfoUpdateRequest;
import com.github.thundax.modules.sys.controller.response.PersonalAvatarResponse;
import com.github.thundax.modules.sys.controller.response.PersonalInfoResponse;
import com.github.thundax.modules.sys.controller.response.PersonalMenuResponse;
import com.github.thundax.modules.sys.controller.response.PersonalPermsResponse;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.utils.AvatarUtils;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class PersonalInterfaceAssembler {
    private PersonalInterfaceAssembler() {}

    @NonNull
    public static PersonalInfoResponse toInfoResponse(User entity, String loginName) {
        if (entity == null) {
            return new PersonalInfoResponse();
        }
        PersonalInfoResponse response = new PersonalInfoResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setLoginName(loginName);
        response.setRanks(AccessRankCodec.toValue(entity.getRank()));
        response.setName(entity.getName());
        response.setMobile(entity.getMobile());
        response.setEmail(entity.getEmail());
        response.setAvatar(readAvatarUrl(entity));
        response.setAdmin(entity.isAdmin());
        response.setSuperAdmin(entity.isSuper());
        return response;
    }

    @NonNull
    public static PersonalAvatarResponse toAvatarResponse(User entity) {
        PersonalAvatarResponse response = new PersonalAvatarResponse();
        response.setAvatar(readAvatarUrl(entity));
        return response;
    }

    @NonNull
    public static PersonalMenuResponse toMenuResponse(Menu entity) {
        if (entity == null) {
            return new PersonalMenuResponse();
        }
        PersonalMenuResponse response = new PersonalMenuResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setParentId(entity.getParentId());
        response.setName(entity.getName());
        response.setPriority(entity.getPriority());
        response.setUrl(entity.getUrl());
        response.setDisplayParams(entity.getDisplayParams());
        return response;
    }

    @NonNull
    public static PersonalPermsResponse toPermsResponse(Set<String> perms) {
        PersonalPermsResponse response = new PersonalPermsResponse();
        response.setPerms(perms);
        return response;
    }

    @NonNull
    public static User toEntity(@NonNull User entity, @NonNull PersonalInfoUpdateRequest request) {
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setMobile(request.getMobile());
        return entity;
    }

    private static String readAvatarUrl(User entity) {
        String id = entity == null ? null : EntityIdCodec.toValue(entity.getId());
        if (StringUtils.isBlank(id) || !AvatarUtils.existAvatar(id)) {
            return null;
        }
        return UserController.getAvatarUrl(id, UserAccessHolder.currentToken());
    }
}
