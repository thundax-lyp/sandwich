package com.github.thundax.modules.auth.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.auth.service.command.AdminAuthCommand;
import com.github.thundax.modules.auth.service.query.AdminAuthQuery;
import com.github.thundax.modules.auth.service.result.AuthAccessTokenResult;
import com.github.thundax.modules.auth.service.result.AuthTokenQueryResult;
import com.github.thundax.modules.auth.service.result.AuthTokenRefreshResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationDecisionResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationViewResult;
import com.github.thundax.modules.sys.entity.User;
import org.springframework.lang.NonNull;

public interface AdminAuthService {

    @NonNull
    AuthAccessTokenResult createAccessToken(AdminAuthCommand command);

    AuthAccessTokenResult getAccessToken(AdminAuthQuery query);

    int deleteAccessTokensByUserId(AdminAuthCommand command);

    boolean validateToken(AdminAuthCommand command);

    void activeAccessToken(AdminAuthCommand command);

    void deleteAccessToken(AdminAuthCommand command);

    AuthTokenQueryResult getTokenInfo(AdminAuthQuery query);

    AuthTokenRefreshResult refreshAccessToken(AdminAuthCommand command) throws ApiException;

    OAuth2AuthorizationViewResult authorizeOAuth2(AdminAuthCommand command) throws ApiException;

    OAuth2AuthorizationDecisionResult decideOAuth2(AdminAuthCommand command) throws ApiException;

    AuthTokenRefreshResult exchangeOAuth2Token(AdminAuthCommand command) throws ApiException;

    boolean revokeAuthorizationCode(AdminAuthCommand command) throws ApiException;

    boolean revokeOAuth2Token(AdminAuthCommand command) throws ApiException;

    void invalidateSessionByToken(AdminAuthCommand command);

    @LayerPublicApi(reason = "账号状态变化时按用户维度失效在线会话的业务入口")
    int invalidateSessionsByUserId(AdminAuthCommand command);

    User authenticatePassword(AdminAuthCommand command) throws ApiException;

    User authenticateSms(AdminAuthCommand command) throws ApiException;

    User authenticateWecom(AdminAuthCommand command) throws ApiException;

    User authenticateGithub(AdminAuthCommand command) throws ApiException;

    void recordLoginFailed(AdminAuthCommand command);

    void validatePassword(AdminAuthCommand command) throws ApiException;
}
