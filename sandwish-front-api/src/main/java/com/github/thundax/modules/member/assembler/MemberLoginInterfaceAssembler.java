package com.github.thundax.modules.member.assembler;

import com.github.thundax.modules.member.controller.response.MemberLoginStatusResponse;
import com.github.thundax.modules.member.security.MemberSpringPrincipal;
import org.springframework.lang.NonNull;

public final class MemberLoginInterfaceAssembler {
    private MemberLoginInterfaceAssembler() {}

    @NonNull
    public static MemberLoginStatusResponse toLoginStatusResponse(MemberSpringPrincipal principal) {
        MemberLoginStatusResponse response = new MemberLoginStatusResponse();
        response.setLoggedIn(principal != null);
        if (principal != null) {
            response.setMemberId(principal.getId());
        }
        return response;
    }

    @NonNull
    public static MemberLoginStatusResponse toLoginFailureResponse(String message) {
        MemberLoginStatusResponse response = new MemberLoginStatusResponse();
        response.setLoggedIn(false);
        response.setMessage(message);
        return response;
    }

    @NonNull
    public static MemberLoginStatusResponse toLogoutResponse() {
        MemberLoginStatusResponse response = new MemberLoginStatusResponse();
        response.setLoggedIn(false);
        response.setMessage("退出成功");
        return response;
    }
}
