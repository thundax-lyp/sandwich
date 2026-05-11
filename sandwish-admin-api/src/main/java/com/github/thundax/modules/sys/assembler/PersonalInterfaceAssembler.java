package com.github.thundax.modules.sys.assembler;

import com.github.thundax.modules.sys.codec.AccessRankCodec;
import com.github.thundax.modules.sys.controller.request.PersonalInfoUpdateRequest;
import com.github.thundax.modules.sys.controller.response.PersonalAvatarResponse;
import com.github.thundax.modules.sys.controller.response.PersonalInfoResponse;
import com.github.thundax.modules.sys.controller.response.PersonalMenuResponse;
import com.github.thundax.modules.sys.controller.response.PersonalPermsResponse;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.valueobject.MenuIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import java.util.Set;
import org.springframework.lang.NonNull;

public final class PersonalInterfaceAssembler {
    private PersonalInterfaceAssembler() {}

    @NonNull
    public static PersonalInfoResponse toInfoResponse(User entity, String loginName, String avatarUrl) {
        if (entity == null) {
            return PersonalInfoResponse.builder().build();
        }
        return PersonalInfoResponse.builder()
                .id(UserIdCodec.toValue(entity.getId()))
                .loginName(loginName)
                .ranks(AccessRankCodec.toValue(entity.getRank()))
                .name(entity.getName())
                .mobile(entity.getMobile())
                .email(entity.getEmail())
                .avatar(avatarUrl)
                .admin(entity.isAdmin())
                .superAdmin(entity.isSuper())
                .build();
    }

    @NonNull
    public static PersonalAvatarResponse toAvatarResponse(String avatarUrl) {
        return PersonalAvatarResponse.builder().avatar(avatarUrl).build();
    }

    @NonNull
    public static PersonalMenuResponse toMenuResponse(Menu entity) {
        if (entity == null) {
            return PersonalMenuResponse.builder().build();
        }
        return PersonalMenuResponse.builder()
                .id(MenuIdCodec.toValue(entity.getId()))
                .parentId(MenuIdCodec.toValue(entity.getParentId()))
                .name(entity.getName())
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
}
