package com.github.thundax.modules.member.assembler;

import com.github.thundax.modules.member.response.MemberLoginStatusResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class MemberLoginInterfaceAssembler {

    @NonNull
    public MemberLoginStatusResponse toLoginStatusResponse(boolean loggedIn) {
        MemberLoginStatusResponse response = new MemberLoginStatusResponse();
        response.setLoggedIn(loggedIn);
        return response;
    }
}
