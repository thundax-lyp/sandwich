package com.github.thundax.modules.auth.assembler;

import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.request.AuthLoginRequest;
import com.github.thundax.modules.auth.response.AuthAccessTokenResponse;
import com.github.thundax.modules.auth.response.AuthLoginFormResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class AuthInterfaceAssembler {

    @NonNull
    public AuthLoginFormResponse toLoginFormResponse(LoginForm entity) {
        if (entity == null) {
            return new AuthLoginFormResponse();
        }

        AuthLoginFormResponse response = new AuthLoginFormResponse();
        response.setLoginToken(entity.getLoginToken());
        response.setRefreshToken(entity.getRefreshTokenList().get(0));
        response.setExpireSeconds(entity.getExpiredSeconds());
        response.setPublicKey(entity.getPublicKey());
        return response;
    }

    @NonNull
    public AuthAccessTokenResponse toAccessTokenResponse(AccessToken entity) {
        AuthAccessTokenResponse response = new AuthAccessTokenResponse();
        if (entity != null) {
            response.setToken(entity.getToken());
        }
        return response;
    }

    public String toLogJson(AuthLoginRequest request) {
        if (request == null) {
            return null;
        }

        AuthLoginRequest maskedRequest = new AuthLoginRequest();
        maskedRequest.setLoginToken(request.getLoginToken());
        maskedRequest.setUsername(request.getUsername());
        maskedRequest.setPassword("******");
        maskedRequest.setCaptcha(request.getCaptcha());
        return JsonUtils.toJson(maskedRequest);
    }
}
