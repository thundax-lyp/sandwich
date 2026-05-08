package com.github.thundax.modules.auth.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.enums.PrincipalAuthenticationMethod;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.service.result.AuthAccessTokenResult;
import com.github.thundax.modules.auth.service.result.AuthTokenQueryResult;
import com.github.thundax.modules.auth.service.result.AuthTokenRefreshResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationDecisionResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationViewResult;
import com.github.thundax.modules.sys.entity.User;
import java.util.List;
import org.springframework.lang.NonNull;

public interface AdminAuthService {

    @NonNull
    AuthAccessTokenResult createAccessToken(String userId);

    @NonNull
    AuthAccessTokenResult createAccessToken(String userId, String loginName);

    @NonNull
    default AuthAccessTokenResult createAccessToken(String userId, String loginName, String ip, String userAgent) {
        return createAccessToken(userId, loginName);
    }

    @NonNull
    default AuthAccessTokenResult createAccessToken(
            String userId,
            String loginName,
            String ip,
            String userAgent,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType) {
        return createAccessToken(userId, loginName, ip, userAgent);
    }

    AuthAccessTokenResult getAccessToken(String token);

    int deleteAccessTokensByUserId(String userId);

    boolean validateToken(AuthAccessTokenResult accessToken);

    void activeAccessToken(AuthAccessTokenResult accessToken);

    void deleteAccessToken(AuthAccessTokenResult accessToken);

    default void deleteAccessToken(AuthAccessTokenResult accessToken, String ip, String userAgent) {
        deleteAccessToken(accessToken);
    }

    AuthTokenQueryResult queryToken(String token);

    AuthTokenRefreshResult refreshAccessToken(String clientId, String refreshToken) throws ApiException;

    default AuthTokenRefreshResult refreshAccessToken(String clientId, String refreshToken, String ip, String userAgent)
            throws ApiException {
        return refreshAccessToken(clientId, refreshToken);
    }

    OAuth2AuthorizationViewResult authorizeOAuth2(
            String clientId, String redirectUri, List<String> scopes, String state) throws ApiException;

    OAuth2AuthorizationDecisionResult decideOAuth2(
            String clientId,
            String redirectUri,
            List<String> scopes,
            String state,
            String codeChallenge,
            String codeChallengeMethod,
            String userId,
            boolean approved)
            throws ApiException;

    default OAuth2AuthorizationDecisionResult decideOAuth2(
            String clientId,
            String redirectUri,
            List<String> scopes,
            String state,
            String codeChallenge,
            String codeChallengeMethod,
            String userId,
            boolean approved,
            String ip,
            String userAgent)
            throws ApiException {
        return decideOAuth2(clientId, redirectUri, scopes, state, codeChallenge, codeChallengeMethod, userId, approved);
    }

    AuthTokenRefreshResult exchangeOAuth2Token(
            String clientId,
            String clientSecret,
            String grantType,
            String redirectUri,
            String authorizationCode,
            String codeVerifier,
            String refreshToken)
            throws ApiException;

    default AuthTokenRefreshResult exchangeOAuth2Token(
            String clientId,
            String clientSecret,
            String grantType,
            String redirectUri,
            String authorizationCode,
            String codeVerifier,
            String refreshToken,
            String ip,
            String userAgent)
            throws ApiException {
        return exchangeOAuth2Token(
                clientId, clientSecret, grantType, redirectUri, authorizationCode, codeVerifier, refreshToken);
    }

    boolean revokeAuthorizationCode(String authorizationCode) throws ApiException;

    boolean revokeOAuth2Token(String clientId, String clientSecret, String token) throws ApiException;

    void invalidateSessionByToken(String token, String reason);

    @LayerPublicApi(reason = "账号状态变化时按用户维度失效在线会话的业务入口")
    int invalidateSessionsByUserId(EntityId userId, String reason);

    /**
     * 账号密码认证。
     *
     * @param loginName 登录名
     * @param plainPassword 明文密码
     * @return 认证通过的用户
     * @throws ApiException 业务异常
     */
    User authenticatePassword(String loginName, String plainPassword) throws ApiException;

    default User authenticatePassword(String loginName, String plainPassword, String ip, String userAgent)
            throws ApiException {
        return authenticatePassword(loginName, plainPassword);
    }

    User authenticateSms(String mobile) throws ApiException;

    default User authenticateSms(String mobile, String ip, String userAgent) throws ApiException {
        return authenticateSms(mobile);
    }

    User authenticateWecom(String code) throws ApiException;

    default User authenticateWecom(String code, String ip, String userAgent) throws ApiException {
        return authenticateWecom(code);
    }

    User authenticateGithub(String code) throws ApiException;

    default User authenticateGithub(String code, String ip, String userAgent) throws ApiException {
        return authenticateGithub(code);
    }

    default void recordLoginFailed(
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            String ip,
            String userAgent,
            String reason) {}

    /**
     * 校验登录密码并处理失败锁定。
     *
     * @param user 用户
     * @param plainPassword 明文密码
     * @throws ApiException 业务异常
     */
    void validatePassword(User user, String plainPassword) throws ApiException;
}
