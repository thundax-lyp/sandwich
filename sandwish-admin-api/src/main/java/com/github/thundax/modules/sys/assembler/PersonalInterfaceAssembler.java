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
            return PersonalInfoResponse.builder().build();
        }
        return PersonalInfoResponse.builder()
                .id(EntityIdCodec.toValue(entity.getId()))
                .loginName(loginName)
                .ranks(AccessRankCodec.toValue(entity.getRank()))
                .name(entity.getName())
                .mobile(entity.getMobile())
                .email(entity.getEmail())
                .avatar(readAvatarUrl(entity))
                .admin(entity.isAdmin())
                .superAdmin(entity.isSuper())
                .build();
    }

    @NonNull
    public static PersonalAvatarResponse toAvatarResponse(User entity) {
        return PersonalAvatarResponse.builder().avatar(readAvatarUrl(entity)).build();
    }

    @NonNull
    public static PersonalMenuResponse toMenuResponse(Menu entity) {
        if (entity == null) {
            return PersonalMenuResponse.builder().build();
        }
        return PersonalMenuResponse.builder()
                .id(EntityIdCodec.toValue(entity.getId()))
                .parentId(EntityIdCodec.toValue(entity.getParentId()))
                .name(entity.getName())
                .priority(entity.getPriority())
                .url(entity.getUrl())
                .displayParams(entity.getDisplayParams())
                .build();
    }

    @NonNull
    public static PersonalPermsResponse toPermsResponse(Set<String> perms) {
        return PersonalPermsResponse.builder().perms(perms).build();
    }

    @NonNull
    public static User toEntity(@NonNull User entity, @NonNull PersonalInfoUpdateRequest request) {
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setMobile(request.getMobile());
        return entity;
    }

    private static String readAvatarUrl(User entity) {
        String id = entity == null ? null : EntityIdCodec.toStringValue(entity.getId());
        if (StringUtils.isBlank(id) || !AvatarUtils.existAvatar(id)) {
            return null;
        }
        return UserController.getAvatarUrl(id, UserAccessHolder.currentToken());
    }
}
