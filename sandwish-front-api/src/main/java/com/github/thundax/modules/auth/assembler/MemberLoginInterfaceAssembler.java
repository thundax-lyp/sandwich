package com.github.thundax.modules.auth.assembler;

import com.github.thundax.modules.auth.controller.response.MemberLoginFormResponse;
import com.github.thundax.modules.auth.controller.response.MemberLoginStatusResponse;
import com.github.thundax.modules.auth.controller.response.MemberTokenResponse;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.security.MemberSpringPrincipal;
import com.github.thundax.modules.auth.service.result.MemberTokenResult;
import org.springframework.lang.NonNull;

public final class MemberLoginInterfaceAssembler {
    private static final String PUBLIC_KEY_ITEM = "publicKey";

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

    public static MemberLoginFormResponse toLoginFormResponse(PreAuthSession session) {
        MemberLoginFormResponse response = new MemberLoginFormResponse();
        response.setLoginToken(session.getToken().asString());
        response.setRefreshToken(session.getRefreshToken().asString());
        response.setExpiredAt(session.getExpiredAt());
        response.setPublicKey(session.findValue(PUBLIC_KEY_ITEM));
        return response;
    }

    public static MemberTokenResponse toTokenResponse(MemberTokenResult result) {
        MemberTokenResponse response = new MemberTokenResponse();
        response.setMemberId(
                result.getMemberId() == null
                        ? null
                        : String.valueOf(result.getMemberId().value()));
        response.setAccessToken(result.getAccessToken());
        response.setRefreshToken(result.getRefreshToken());
        response.setExpiresIn(result.getExpiresIn());
        return response;
    }
}
