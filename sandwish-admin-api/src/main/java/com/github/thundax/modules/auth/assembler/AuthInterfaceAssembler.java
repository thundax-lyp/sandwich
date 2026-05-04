package com.github.thundax.modules.auth.assembler;

import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.auth.controller.request.AuthLoginRequest;
import com.github.thundax.modules.auth.controller.response.AuthAccessTokenResponse;
import com.github.thundax.modules.auth.controller.response.AuthLoginFormResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2IntrospectionResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2UserinfoResponse;
import com.github.thundax.modules.auth.controller.response.TokenVerifyResponse;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.service.result.AuthTokenQueryResult;
import com.github.thundax.modules.auth.service.result.AuthTokenRefreshResult;
import com.github.thundax.modules.sys.entity.User;
import org.springframework.lang.NonNull;

public final class AuthInterfaceAssembler {
    private AuthInterfaceAssembler() {}

    @NonNull
    public static AuthLoginFormResponse toLoginFormResponse(LoginForm entity) {
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
    public static AuthAccessTokenResponse toAccessTokenResponse(AccessToken entity) {
        AuthAccessTokenResponse response = new AuthAccessTokenResponse();
        if (entity != null) {
            response.setToken(entity.getToken());
        }
        return response;
    }

    @NonNull
    public static AuthAccessTokenResponse toAccessTokenResponse(AuthTokenRefreshResult result) {
        AuthAccessTokenResponse response = new AuthAccessTokenResponse();
        if (result != null && result.getAccessToken() != null) {
            response.setToken(result.getAccessToken().getToken());
            response.setRefreshToken(result.getRefreshToken());
        }
        return response;
    }

    public static String toLogJson(AuthLoginRequest request) {
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

    @NonNull
    public static TokenVerifyResponse toTokenVerifyResponse(AuthTokenQueryResult result) {
        TokenVerifyResponse response = new TokenVerifyResponse();
        response.setActive(result != null && result.isActive());
        return response;
    }

    @NonNull
    public static OAuth2IntrospectionResponse toIntrospectionResponse(AuthTokenQueryResult result) {
        OAuth2IntrospectionResponse response = new OAuth2IntrospectionResponse();
        if (result == null || !result.isActive()) {
            response.setActive(false);
            return response;
        }
        response.setActive(true);
        response.setSubject(userId(result.getUser()));
        response.setUsername(username(result.getUser()));
        if (result.getSession() != null) {
            response.setSessionId(result.getSession().getSessionId());
        }
        return response;
    }

    @NonNull
    public static OAuth2UserinfoResponse toUserinfoResponse(AuthTokenQueryResult result) {
        OAuth2UserinfoResponse response = new OAuth2UserinfoResponse();
        if (result == null || !result.isActive()) {
            return response;
        }
        response.setSubject(userId(result.getUser()));
        response.setUsername(username(result.getUser()));
        response.setName(result.getUser() == null ? null : result.getUser().getName());
        return response;
    }

    private static String userId(User user) {
        return user == null || user.getId() == null ? null : user.getId().value();
    }

    private static String username(User user) {
        return user == null ? null : user.getLoginName();
    }
}
