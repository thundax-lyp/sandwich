package com.github.thundax.modules.auth.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.controller.response.MemberRegisterResponse;

public final class MemberRegisterInterfaceAssembler {

    private MemberRegisterInterfaceAssembler() {}

    public static MemberRegisterResponse toRegisterResponse(EntityId memberId) {
        MemberRegisterResponse response = new MemberRegisterResponse();
        response.setSuccess(true);
        response.setMemberId(EntityIdCodec.toStringValue(memberId));
        response.setMessage("注册成功");
        return response;
    }

    public static MemberRegisterResponse toCodeResponse() {
        MemberRegisterResponse response = new MemberRegisterResponse();
        response.setSuccess(true);
        response.setMessage("验证码已发送");
        return response;
    }
}
