package com.github.thundax.modules.auth.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.controller.response.MemberRegisterResponse;

public final class MemberRegisterInterfaceAssembler {

    private MemberRegisterInterfaceAssembler() {}

    public static MemberRegisterResponse toRegisterResponse(EntityId memberId) {
        return MemberRegisterResponse.builder()
                .success(true)
                .memberId(EntityIdCodec.toStringValue(memberId))
                .message("注册成功")
                .build();
    }

    public static MemberRegisterResponse toCodeResponse() {
        return MemberRegisterResponse.builder().success(true).message("验证码已发送").build();
    }
}
