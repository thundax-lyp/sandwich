package com.github.thundax.modules.member.assembler;

import com.github.thundax.modules.member.controller.response.MemberLoginStatusResponse;
import org.springframework.lang.NonNull;

public final class MemberLoginInterfaceAssembler {
    private MemberLoginInterfaceAssembler() {}

    @NonNull
    public static MemberLoginStatusResponse toLoginStatusResponse(boolean loggedIn) {
        MemberLoginStatusResponse response = new MemberLoginStatusResponse();
        response.setLoggedIn(loggedIn);
        return response;
    }
}
