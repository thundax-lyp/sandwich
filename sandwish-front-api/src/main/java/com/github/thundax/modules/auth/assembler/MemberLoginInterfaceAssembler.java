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
        return MemberLoginStatusResponse.builder()
                .loggedIn(principal != null)
                .memberId(principal == null ? null : principal.getId())
                .build();
    }

    @NonNull
    public static MemberLoginStatusResponse toLoginFailureResponse(String message) {
        return MemberLoginStatusResponse.builder()
                .loggedIn(false)
                .message(message)
                .build();
    }

    @NonNull
    public static MemberLoginStatusResponse toLogoutResponse() {
        return MemberLoginStatusResponse.builder()
                .loggedIn(false)
                .message("退出成功")
                .build();
    }

    public static MemberLoginFormResponse toLoginFormResponse(PreAuthSession session) {
        return MemberLoginFormResponse.builder()
                .loginToken(session.getToken().asString())
                .refreshToken(session.getRefreshToken().asString())
                .expiredAt(session.getExpiredAt())
                .publicKey(session.findValue(PUBLIC_KEY_ITEM))
                .build();
    }

    public static MemberTokenResponse toTokenResponse(MemberTokenResult result) {
        return MemberTokenResponse.builder()
                .memberId(
                        result.getMemberId() == null
                                ? null
                                : String.valueOf(result.getMemberId().value()))
                .accessToken(result.getAccessToken())
                .refreshToken(result.getRefreshToken())
                .expiresIn(result.getExpiresIn())
                .build();
    }
}
