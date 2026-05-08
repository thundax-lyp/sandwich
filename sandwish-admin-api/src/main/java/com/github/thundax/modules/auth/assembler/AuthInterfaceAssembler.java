package com.github.thundax.modules.auth.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.auth.controller.request.AuthLoginRequest;
import com.github.thundax.modules.auth.controller.response.AuthAccessTokenResponse;
import com.github.thundax.modules.auth.controller.response.AuthLoginFormResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2AuthorizationDecisionResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2AuthorizationViewResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2IntrospectionResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2UserinfoResponse;
import com.github.thundax.modules.auth.controller.response.TokenVerifyResponse;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.service.result.AuthAccessTokenResult;
import com.github.thundax.modules.auth.service.result.AuthTokenQueryResult;
import com.github.thundax.modules.auth.service.result.AuthTokenRefreshResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationDecisionResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationViewResult;
import com.github.thundax.modules.sys.entity.User;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.springframework.lang.NonNull;

public final class AuthInterfaceAssembler {
    private static final String PUBLIC_KEY_ITEM = "publicKey";

    private AuthInterfaceAssembler() {}

    @NonNull
    public static AuthLoginFormResponse toLoginFormResponse(PreAuthSession session) {
        if (session == null) {
            return new AuthLoginFormResponse();
        }
        AuthLoginFormResponse response = new AuthLoginFormResponse();
        response.setLoginToken(session.getToken().asString());
        response.setRefreshToken(session.getRefreshToken().asString());
        response.setExpiredAt(session.getExpiredAt());
        response.setPublicKey(session.findValue(PUBLIC_KEY_ITEM));
        return response;
    }

    @NonNull
    public static AuthAccessTokenResponse toAccessTokenResponse(AuthAccessTokenResult entity) {
        AuthAccessTokenResponse response = new AuthAccessTokenResponse();
        if (entity != null) {
            response.setToken(entity.getToken());
            response.setRefreshToken(entity.getRefreshToken());
        }
        return response;
    }

    @NonNull
    public static AuthAccessTokenResponse toAccessTokenResponse(AuthTokenRefreshResult result) {
        AuthAccessTokenResponse response = new AuthAccessTokenResponse();
        if (result != null && result.getAccessToken() != null) {
            response.setToken(
                    result.getOauthAccessToken() == null
                            ? result.getAccessToken().getToken()
                            : result.getOauthAccessToken());
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

    public static String toLogJson(String loginName) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("loginName", loginName);
        return JsonUtils.toJson(request);
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
        response.setUsername(result.getUsername());
        PrincipalAccessToken principalAccessToken = result.getPrincipalAccessToken();
        if (principalAccessToken != null) {
            response.setClientId(principalAccessToken.getClientId());
            response.setScope(scope(principalAccessToken.getScopes()));
            response.setExpiresAt(epochSeconds(principalAccessToken.getExpireAt()));
            response.setTokenType("Bearer");
        }
        if (result.getSession() != null) {
            response.setSessionId(
                    EntityIdCodec.toStringValue(result.getSession().getId()));
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
        response.setUsername(result.getUsername());
        response.setPreferredUsername(result.getUsername());
        response.setName(result.getUser() == null ? null : result.getUser().getName());
        return response;
    }

    @NonNull
    public static OAuth2AuthorizationViewResponse toAuthorizationViewResponse(OAuth2AuthorizationViewResult result) {
        OAuth2AuthorizationViewResponse response = new OAuth2AuthorizationViewResponse();
        if (result != null) {
            response.setClientId(result.getClientId());
            response.setClientName(result.getClientName());
            response.setRedirectUri(result.getRedirectUri());
            response.setScopes(result.getScopes());
            response.setState(result.getState());
        }
        return response;
    }

    @NonNull
    public static OAuth2AuthorizationDecisionResponse toAuthorizationDecisionResponse(
            OAuth2AuthorizationDecisionResult result) {
        OAuth2AuthorizationDecisionResponse response = new OAuth2AuthorizationDecisionResponse();
        if (result != null) {
            response.setApproved(result.isApproved());
            response.setAuthorizationCode(result.getAuthorizationCode());
            response.setState(result.getState());
        }
        return response;
    }

    private static String userId(User user) {
        return user == null || user.getId() == null
                ? null
                : String.valueOf(user.getId().value());
    }

    private static String scope(Set<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(" ");
        for (String scope : scopes) {
            joiner.add(scope);
        }
        return joiner.toString();
    }

    private static Long epochSeconds(Date date) {
        return date == null ? null : date.getTime() / 1000L;
    }
}
