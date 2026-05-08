package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.autoconfigure.LoginProperties;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.UuidHelper;
import com.github.thundax.common.utils.encrypt.Sha256Helper;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.AccessTokenDao;
import com.github.thundax.modules.auth.dao.AuthSessionDao;
import com.github.thundax.modules.auth.dao.AuthSessionRuntimeDao;
import com.github.thundax.modules.auth.dao.OAuthAccessTokenDao;
import com.github.thundax.modules.auth.dao.OAuthAuthorizationDao;
import com.github.thundax.modules.auth.dao.OAuthClientDao;
import com.github.thundax.modules.auth.dao.OAuthRefreshTokenDao;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.entity.AuthSession;
import com.github.thundax.modules.auth.entity.OAuthAccessToken;
import com.github.thundax.modules.auth.entity.OAuthAuthorization;
import com.github.thundax.modules.auth.entity.OAuthClient;
import com.github.thundax.modules.auth.entity.OAuthRefreshToken;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.AuthSessionStatus;
import com.github.thundax.modules.auth.entity.enums.OAuthClientStatus;
import com.github.thundax.modules.auth.entity.enums.OAuthRefreshTokenStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.exception.BannedAccountException;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.exception.InvalidUsernamePasswordException;
import com.github.thundax.modules.auth.service.AdminAuthService;
import com.github.thundax.modules.auth.service.PermissionService;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.PrincipalAuthService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.service.dto.PreAuthSessionDTO;
import com.github.thundax.modules.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.github.thundax.modules.auth.service.provider.GithubLoginProvider;
import com.github.thundax.modules.auth.service.provider.WecomLoginProvider;
import com.github.thundax.modules.auth.service.result.AuthTokenQueryResult;
import com.github.thundax.modules.auth.service.result.AuthTokenRefreshResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationDecisionResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationViewResult;
import com.github.thundax.modules.auth.utils.AuthUtils;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.UserService;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final int SESSION_RUNTIME_SAFETY_SECONDS = 10;

    private final AuthProperties properties;
    private final LoginProperties loginProperties;
    private final PreAuthSessionService preAuthSessionService;
    private final AccessTokenDao accessTokenDao;
    private final AuthSessionDao authSessionDao;
    private final AuthSessionRuntimeDao authSessionRuntimeDao;
    private final PermissionService permissionService;
    private final PrincipalAuthService principalAuthService;
    private final PrincipalIdentityService principalIdentityService;
    private final UserService userService;

    @Autowired(required = false)
    private WecomLoginProvider wecomLoginProvider;

    @Autowired(required = false)
    private GithubLoginProvider githubLoginProvider;

    @Autowired(required = false)
    private OAuthAuthorizationDao oauthAuthorizationDao;

    @Autowired(required = false)
    private OAuthAccessTokenDao oauthAccessTokenDao;

    @Autowired(required = false)
    private OAuthClientDao oauthClientDao;

    @Autowired(required = false)
    private OAuthRefreshTokenDao oauthRefreshTokenDao;

    public AdminAuthServiceImpl(
            AuthProperties properties,
            LoginProperties loginProperties,
            PreAuthSessionService preAuthSessionService,
            AccessTokenDao accessTokenDao,
            AuthSessionDao authSessionDao,
            AuthSessionRuntimeDao authSessionRuntimeDao,
            PermissionService permissionService,
            PrincipalAuthService principalAuthService,
            PrincipalIdentityService principalIdentityService,
            UserService userService) {
        this.properties = properties;
        this.loginProperties = loginProperties;
        this.preAuthSessionService = preAuthSessionService;
        this.accessTokenDao = accessTokenDao;
        this.authSessionDao = authSessionDao;
        this.authSessionRuntimeDao = authSessionRuntimeDao;
        this.permissionService = permissionService;
        this.principalAuthService = principalAuthService;
        this.principalIdentityService = principalIdentityService;
        this.userService = userService;
    }

    @Override
    public PreAuthSessionDTO createPreAuthSession() throws ApiException {
        return preAuthSessionService.createPreAuthSession(PrincipalType.USER);
    }

    @Override
    public PreAuthSessionDTO refreshPreAuthSession(String refreshToken) throws ApiException {
        return preAuthSessionService.refreshPreAuthSession(PrincipalType.USER, refreshToken);
    }

    @Override
    public void releasePreAuthSession(String loginToken) {
        preAuthSessionService.releasePreAuthSession(PrincipalType.USER, loginToken);
    }

    @Override
    public String createCaptcha(String loginToken) throws InvalidTokenException {
        try {
            return preAuthSessionService.createCaptcha(PrincipalType.USER, loginToken);
        } catch (ApiException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getCaptcha(String loginToken) throws InvalidTokenException, InvalidCaptchaException {
        try {
            return preAuthSessionService.getCaptcha(PrincipalType.USER, loginToken);
        } catch (ApiException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean validateCaptcha(String loginToken, String captcha)
            throws InvalidTokenException, InvalidCaptchaException {
        try {
            return preAuthSessionService.validateCaptcha(PrincipalType.USER, loginToken, captcha);
        } catch (ApiException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String createSmsValidateCode(String loginToken, String mobile) throws InvalidTokenException {
        try {
            return preAuthSessionService.createSmsValidateCode(PrincipalType.USER, loginToken, mobile);
        } catch (ApiException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getSmsValidateCode(String loginToken) throws InvalidTokenException, InvalidCaptchaException {
        try {
            return preAuthSessionService.getSmsValidateCode(PrincipalType.USER, loginToken);
        } catch (ApiException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean validateSmsValidateCode(String loginToken, String mobile, String validateCode)
            throws InvalidTokenException, InvalidCaptchaException {
        try {
            return preAuthSessionService.validateSmsValidateCode(PrincipalType.USER, loginToken, mobile, validateCode);
        } catch (ApiException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    @NonNull
    public AccessToken createAccessToken(String userId) {
        return createAccessToken(userId, null);
    }

    @Override
    @NonNull
    public AccessToken createAccessToken(String userId, String loginName) {
        String token = UUID.randomUUID().toString();

        AccessToken accessToken = new AccessToken();
        accessToken.setToken(token);
        accessToken.setUserId(userId);
        accessToken.setCheckCode(AuthUtils.currentCheckCode());

        accessTokenDao.insert(accessToken);
        permissionService.createSession(token, userId);
        createAuthSession(accessToken, loginName);

        return accessToken;
    }

    @Override
    public AccessToken getAccessToken(String token) {
        String userId = accessTokenDao.getUidByToken(token);
        if (StringUtils.isEmpty(userId)) {
            return null;
        }

        return accessTokenDao.getByUserId(userId);
    }

    @Override
    public AccessToken getByUserId(String userId) {
        return accessTokenDao.getByUserId(userId);
    }

    @Override
    public boolean validateToken(AccessToken accessToken) {
        return AuthUtils.validateCheckCode(accessToken.getCheckCode());
    }

    @Override
    public void activeAccessToken(AccessToken accessToken) {
        accessTokenDao.active(accessToken);
        permissionService.touch(accessToken.getToken());
        touchAuthSession(accessToken.getToken());
    }

    @Override
    public void deleteAccessToken(AccessToken accessToken) {
        accessTokenDao.deleteByToken(accessToken.getToken());
        permissionService.release(accessToken.getToken());
        logoutAuthSession(accessToken.getToken());
    }

    @Override
    public AuthTokenQueryResult queryToken(String token) {
        AuthTokenQueryResult oauthResult = queryOAuthAccessToken(token);
        if (oauthResult != null) {
            return oauthResult;
        }
        AccessToken accessToken = getAccessToken(token);
        if (accessToken == null || !validateToken(accessToken)) {
            return AuthTokenQueryResult.inactive(token);
        }
        AuthSession session = authSessionRuntimeDao.getByToken(token);
        if (session == null) {
            session = authSessionDao.getByToken(token);
        }
        if (session == null || !session.isActive() || session.isExpired(new Date())) {
            return AuthTokenQueryResult.inactive(token);
        }
        User user = userService.getById(session.getUserId());
        if (user == null || !user.isEnable()) {
            return AuthTokenQueryResult.inactive(token);
        }
        return AuthTokenQueryResult.active(token, session, user, getAccountLoginName(user.getId()));
    }

    private AuthTokenQueryResult queryOAuthAccessToken(String token) {
        if (oauthAccessTokenDao == null) {
            return null;
        }
        OAuthAccessToken accessToken = oauthAccessTokenDao.getByTokenHash(tokenHash(token));
        if (accessToken == null) {
            return null;
        }
        if (!accessToken.isIntrospectionActive(new Date())) {
            return AuthTokenQueryResult.inactive(token);
        }
        User user = userService.getById(accessToken.getUserId());
        if (user == null || !user.isEnable()) {
            return AuthTokenQueryResult.inactive(token);
        }
        return AuthTokenQueryResult.active(token, accessToken, user, getAccountLoginName(user.getId()));
    }

    @Override
    public AuthTokenRefreshResult refreshAccessToken(String clientId, String refreshToken) throws ApiException {
        if (oauthRefreshTokenDao == null) {
            throw new ApiException("refresh token 未配置");
        }
        OAuthRefreshToken current = oauthRefreshTokenDao.getByTokenHash(tokenHash(refreshToken));
        Date now = new Date();
        if (current == null || !current.canRefresh(now) || !StringUtils.equals(clientId, current.getClientId())) {
            throw new InvalidTokenException();
        }
        current.markUsed(now);
        oauthRefreshTokenDao.updateStatus(current);

        AccessToken accessToken = createAccessToken(EntityIdCodec.toStringValue(current.getUserId()));
        String nextRefreshToken = createOAuthRefreshToken(accessToken, clientId, now);
        return new AuthTokenRefreshResult(accessToken, nextRefreshToken);
    }

    @Override
    public OAuth2AuthorizationViewResult authorizeOAuth2(
            String clientId, String redirectUri, List<String> scopes, String state) throws ApiException {
        OAuthClient client = validateOAuthClient(clientId, redirectUri, scopes);
        OAuth2AuthorizationViewResult result = new OAuth2AuthorizationViewResult();
        result.setClientId(client.getClientId());
        result.setClientName(client.getClientName());
        result.setRedirectUri(redirectUri);
        result.setScopes(toScopeSet(scopes));
        result.setState(state);
        return result;
    }

    @Override
    public OAuth2AuthorizationDecisionResult decideOAuth2(
            String clientId,
            String redirectUri,
            List<String> scopes,
            String state,
            String codeChallenge,
            String codeChallengeMethod,
            String userId,
            boolean approved)
            throws ApiException {
        validateOAuthClient(clientId, redirectUri, scopes);
        OAuth2AuthorizationDecisionResult result = new OAuth2AuthorizationDecisionResult();
        result.setApproved(approved);
        result.setState(state);
        if (!approved) {
            return result;
        }
        if (oauthAuthorizationDao == null) {
            throw new ApiException("OAuth2 authorization 未配置");
        }
        Date now = new Date();
        OAuthAuthorization authorization = new OAuthAuthorization();
        authorization.setAuthorizationCode(UuidHelper.compact());
        authorization.setClientId(clientId);
        authorization.setUserId(EntityIdCodec.toDomain(Long.valueOf(userId)));
        authorization.setRedirectUri(redirectUri);
        authorization.setScopes(toScopeSet(scopes));
        authorization.setState(state);
        authorization.setCodeChallenge(codeChallenge);
        authorization.setCodeChallengeMethod(codeChallengeMethod);
        authorization.setIssuedAt(now);
        authorization.setExpireAt(new Date(now.getTime() + 300000L));
        authorization.setCreateDate(now);
        authorization.setUpdateDate(now);
        authorization.setId(oauthAuthorizationDao.insert(authorization));
        result.setAuthorizationCode(authorization.getAuthorizationCode());
        return result;
    }

    @Override
    public AuthTokenRefreshResult exchangeOAuth2Token(
            String clientId,
            String clientSecret,
            String grantType,
            String redirectUri,
            String authorizationCode,
            String codeVerifier,
            String refreshToken)
            throws ApiException {
        OAuthClient client = validateOAuthClientSecret(clientId, clientSecret);
        if (!client.supportsGrantType(grantType)) {
            throw new ApiException("OAuth2 grant type unsupported");
        }
        if ("authorization_code".equals(grantType)) {
            return exchangeAuthorizationCode(client, redirectUri, authorizationCode, codeVerifier);
        }
        if ("refresh_token".equals(grantType)) {
            return refreshOAuth2Token(client, refreshToken);
        }
        throw new ApiException("OAuth2 grant type unsupported");
    }

    private AuthTokenRefreshResult exchangeAuthorizationCode(
            OAuthClient client, String redirectUri, String authorizationCode, String codeVerifier) throws ApiException {
        if (oauthAuthorizationDao == null) {
            throw new ApiException("OAuth2 authorization 未配置");
        }
        OAuthAuthorization authorization = oauthAuthorizationDao.getByAuthorizationCode(authorizationCode);
        Date now = new Date();
        if (authorization == null
                || !authorization.canConsume(now)
                || !StringUtils.equals(client.getClientId(), authorization.getClientId())
                || !StringUtils.equals(redirectUri, authorization.getRedirectUri())
                || !verifyPkce(authorization, codeVerifier)) {
            throw new InvalidTokenException();
        }
        authorization.markUsed(now);
        oauthAuthorizationDao.updateUsed(authorization);
        AccessToken accessToken = createAccessToken(EntityIdCodec.toStringValue(authorization.getUserId()));
        String oauthAccessToken = createOAuthAccessToken(accessToken, client, authorization, now);
        String refreshToken =
                oauthRefreshTokenDao == null ? null : createOAuthRefreshToken(accessToken, client.getClientId(), now);
        return new AuthTokenRefreshResult(accessToken, refreshToken, oauthAccessToken);
    }

    private AuthTokenRefreshResult refreshOAuth2Token(OAuthClient client, String refreshToken) throws ApiException {
        if (oauthRefreshTokenDao == null) {
            throw new ApiException("refresh token 未配置");
        }
        OAuthRefreshToken current = oauthRefreshTokenDao.getByTokenHash(tokenHash(refreshToken));
        Date now = new Date();
        if (current == null
                || !current.canRefresh(now)
                || !StringUtils.equals(client.getClientId(), current.getClientId())) {
            throw new InvalidTokenException();
        }
        current.markUsed(now);
        oauthRefreshTokenDao.updateStatus(current);

        AccessToken accessToken = createAccessToken(EntityIdCodec.toStringValue(current.getUserId()));
        String oauthAccessToken = createOAuthAccessToken(accessToken, client, current, now);
        String nextRefreshToken = createOAuthRefreshToken(accessToken, client.getClientId(), now);
        return new AuthTokenRefreshResult(accessToken, nextRefreshToken, oauthAccessToken);
    }

    @Override
    public boolean revokeAuthorizationCode(String authorizationCode) throws ApiException {
        if (oauthAuthorizationDao == null) {
            throw new ApiException("OAuth2 authorization 未配置");
        }
        return oauthAuthorizationDao.deleteByAuthorizationCode(authorizationCode) > 0;
    }

    @Override
    public boolean revokeOAuth2Token(String clientId, String clientSecret, String token) throws ApiException {
        validateOAuthClientSecret(clientId, clientSecret);
        Date now = new Date();
        boolean revoked = false;
        if (oauthAccessTokenDao != null) {
            OAuthAccessToken accessToken = oauthAccessTokenDao.getByTokenHash(tokenHash(token));
            if (accessToken != null && accessToken.isActive()) {
                accessToken.revoke(now);
                oauthAccessTokenDao.updateStatus(accessToken);
                revoked = true;
            }
        }
        if (oauthRefreshTokenDao != null) {
            OAuthRefreshToken refreshToken = oauthRefreshTokenDao.getByTokenHash(tokenHash(token));
            if (refreshToken != null && refreshToken.isActive()) {
                refreshToken.revoke(now);
                oauthRefreshTokenDao.updateStatus(refreshToken);
                revoked = true;
            }
        }
        return revoked;
    }

    @Override
    public void invalidateSessionByToken(String token, String reason) {
        invalidateAuthSession(token, reason);
    }

    @Override
    public int invalidateSessionsByUserId(EntityId userId, String reason) {
        List<AuthSession> sessions = authSessionDao.listByUserIdAndStatus(userId, AuthSessionStatus.ACTIVE);
        return invalidateAuthSessions(sessions, reason);
    }

    @Override
    public User authenticatePassword(String loginName, String plainPassword) throws ApiException {
        PrincipalIdentity identity;
        try {
            identity = principalAuthService.authenticatePassword(
                    PrincipalIdentityType.USER_ACCOUNT,
                    loginName,
                    PrincipalCredentialType.USER_PASSWORD,
                    plainPassword,
                    passwordPolicy());
        } catch (InvalidPasswordException e) {
            throw new InvalidUsernamePasswordException();
        }

        User user = userService.getById(identity.getPrincipalKey().getPrincipalId());
        if (user == null) {
            throw new InvalidUsernamePasswordException();
        }
        if (!user.isEnable()) {
            throw new BannedAccountException();
        }
        return user;
    }

    @Override
    public User authenticateSms(String loginToken, String mobile, String validateCode) throws ApiException {
        if (!validateSmsValidateCode(loginToken, mobile, validateCode)) {
            throw new InvalidCaptchaException();
        }
        return authenticateIdentity(PrincipalIdentityType.USER_MOBILE, mobile);
    }

    @Override
    public User authenticateWecom(String code) throws ApiException {
        if (wecomLoginProvider == null) {
            throw new ApiException("企业微信登录未配置");
        }
        return authenticateIdentity(PrincipalIdentityType.USER_WECOM, wecomLoginProvider.resolveIdentity(code));
    }

    @Override
    public User authenticateGithub(String code) throws ApiException {
        if (githubLoginProvider == null) {
            throw new ApiException("GitHub登录未配置");
        }
        return authenticateIdentity(PrincipalIdentityType.USER_GITHUB, githubLoginProvider.resolveIdentity(code));
    }

    @Override
    public void validatePassword(User user, String plainPassword) throws ApiException {
        if (user == null) {
            throw new InvalidUsernamePasswordException();
        }
        String loginName = getAccountLoginName(user.getId());
        if (StringUtils.isBlank(loginName)) {
            throw new InvalidUsernamePasswordException();
        }
        authenticatePassword(loginName, plainPassword);
    }

    @Override
    public String getPrivateKey(String loginToken) throws InvalidTokenException {
        try {
            return preAuthSessionService.getPrivateKey(PrincipalType.USER, loginToken);
        } catch (ApiException e) {
            throw new IllegalStateException(e);
        }
    }

    private User authenticateIdentity(PrincipalIdentityType identityType, String identityValue) throws ApiException {
        PrincipalIdentity identity;
        try {
            identity = principalAuthService.authenticateIdentity(identityType, identityValue);
        } catch (InvalidPasswordException e) {
            throw new InvalidUsernamePasswordException();
        }
        User user = userService.getById(identity.getPrincipalKey().getPrincipalId());
        if (user == null) {
            throw new InvalidUsernamePasswordException();
        }
        if (!user.isEnable()) {
            throw new BannedAccountException();
        }
        return user;
    }

    private void createAuthSession(AccessToken accessToken, String loginName) {
        if (StringUtils.isBlank(loginName)) {
            return;
        }
        PrincipalIdentity identity =
                principalIdentityService.getByIdentity(PrincipalIdentityType.USER_ACCOUNT, loginName);
        if (identity == null
                || identity.getPrincipalKey() == null
                || !StringUtils.equals(
                        accessToken.getUserId(),
                        EntityIdCodec.toStringValue(identity.getPrincipalKey().getPrincipalId()))) {
            return;
        }

        Date now = new Date();
        AuthSession authSession = new AuthSession();
        authSession.setSessionId(UuidHelper.compact());
        authSession.setToken(accessToken.getToken());
        authSession.setUserId(identity.getPrincipalKey().getPrincipalId());
        authSession.setIdentityId(identity.getId());
        authSession.setIdentityType(identity.getType());
        authSession.setLoginType(PrincipalCredentialType.USER_PASSWORD.credentialName());
        authSession.setStatus(AuthSessionStatus.ACTIVE);
        authSession.setIssuedAt(now);
        authSession.setLastAccessTime(now);
        authSession.setExpireAt(new Date(now.getTime() + properties.getLoginExpiredSeconds() * 1000L));
        authSession.setCreateDate(now);
        authSession.setUpdateDate(now);
        authSession.setId(authSessionDao.insert(authSession));
        authSessionRuntimeDao.insert(authSession, runtimeExpiredSeconds());
    }

    private void touchAuthSession(String token) {
        AuthSession authSession = authSessionRuntimeDao.getByToken(token);
        if (authSession == null) {
            return;
        }

        Date now = new Date();
        if (authSession.isExpired(now)) {
            authSession.expire();
            authSessionDao.updateExpire(authSession);
            authSessionRuntimeDao.deleteByToken(token);
            return;
        }
        if (authSession.isActive()) {
            authSessionRuntimeDao.touch(token, now, runtimeExpiredSeconds());
        }
    }

    private void logoutAuthSession(String token) {
        AuthSession runtimeSession = authSessionRuntimeDao.getByToken(token);
        AuthSession authSession = authSessionDao.getByToken(token);
        if (authSession == null || !authSession.isActive()) {
            authSessionRuntimeDao.deleteByToken(token);
            return;
        }

        if (runtimeSession != null && runtimeSession.getLastAccessTime() != null) {
            authSession.touch(runtimeSession.getLastAccessTime());
        }
        authSession.logout(new Date());
        authSessionDao.updateLogout(authSession);
        authSessionRuntimeDao.deleteByToken(token);
    }

    private int invalidateAuthSessions(List<AuthSession> sessions, String reason) {
        if (sessions == null || sessions.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (AuthSession session : sessions) {
            invalidateAuthSession(session.getToken(), reason);
            count++;
        }
        return count;
    }

    private void invalidateAuthSession(String token, String reason) {
        AuthSession runtimeSession = authSessionRuntimeDao.getByToken(token);
        AuthSession authSession = authSessionDao.getByToken(token);
        if (authSession == null || !authSession.isActive()) {
            authSessionRuntimeDao.deleteByToken(token);
            return;
        }

        if (runtimeSession != null && runtimeSession.getLastAccessTime() != null) {
            authSession.touch(runtimeSession.getLastAccessTime());
        }
        authSession.invalidate(reason);
        authSessionDao.updateInvalidate(authSession);
        permissionService.release(token);
        accessTokenDao.deleteByToken(token);
        authSessionRuntimeDao.deleteByToken(token);
    }

    private int runtimeExpiredSeconds() {
        return properties.getLoginExpiredSeconds() + SESSION_RUNTIME_SAFETY_SECONDS;
    }

    private String createOAuthRefreshToken(AccessToken accessToken, String clientId, Date issuedAt) {
        String refreshToken = UuidHelper.compact();
        OAuthRefreshToken entity = new OAuthRefreshToken();
        entity.setTokenId(UuidHelper.compact());
        entity.setTokenHash(tokenHash(refreshToken));
        entity.setAccessTokenId(accessToken.getToken());
        entity.setClientId(clientId);
        entity.setUserId(EntityIdCodec.toDomain(Long.valueOf(accessToken.getUserId())));
        entity.setIssuedAt(issuedAt);
        entity.setExpireAt(new Date(issuedAt.getTime() + refreshTokenTtlSeconds(clientId) * 1000L));
        entity.setStatus(OAuthRefreshTokenStatus.ACTIVE);
        entity.setCreateDate(issuedAt);
        entity.setUpdateDate(issuedAt);
        entity.setId(oauthRefreshTokenDao.insert(entity));
        return refreshToken;
    }

    private String createOAuthAccessToken(
            AccessToken accessToken, OAuthClient client, OAuthAuthorization authorization, Date issuedAt) {
        if (oauthAccessTokenDao == null) {
            return accessToken.getToken();
        }
        String token = UuidHelper.compact();
        OAuthAccessToken entity = new OAuthAccessToken();
        entity.setTokenId(UuidHelper.compact());
        entity.setTokenHash(tokenHash(token));
        entity.setClientId(client.getClientId());
        entity.setUserId(authorization.getUserId());
        entity.setScopes(authorization.getScopes());
        entity.setIssuedAt(issuedAt);
        entity.setExpireAt(new Date(issuedAt.getTime() + accessTokenTtlSeconds(client) * 1000L));
        entity.setCreateDate(issuedAt);
        entity.setUpdateDate(issuedAt);
        entity.setId(oauthAccessTokenDao.insert(entity));
        return token;
    }

    private String createOAuthAccessToken(
            AccessToken accessToken, OAuthClient client, OAuthRefreshToken refreshToken, Date issuedAt) {
        if (oauthAccessTokenDao == null) {
            return accessToken.getToken();
        }
        String token = UuidHelper.compact();
        OAuthAccessToken entity = new OAuthAccessToken();
        entity.setTokenId(UuidHelper.compact());
        entity.setTokenHash(tokenHash(token));
        entity.setClientId(client.getClientId());
        entity.setUserId(refreshToken.getUserId());
        entity.setIssuedAt(issuedAt);
        entity.setExpireAt(new Date(issuedAt.getTime() + accessTokenTtlSeconds(client) * 1000L));
        entity.setCreateDate(issuedAt);
        entity.setUpdateDate(issuedAt);
        entity.setId(oauthAccessTokenDao.insert(entity));
        return token;
    }

    private long refreshTokenTtlSeconds(String clientId) {
        if (oauthClientDao == null) {
            return 2592000L;
        }
        OAuthClient client = oauthClientDao.getByClientIdAndStatus(clientId, OAuthClientStatus.ENABLED);
        if (client == null || client.getRefreshTokenTtlSeconds() <= 0L) {
            return 2592000L;
        }
        return client.getRefreshTokenTtlSeconds();
    }

    private long accessTokenTtlSeconds(OAuthClient client) {
        if (client == null || client.getAccessTokenTtlSeconds() <= 0L) {
            return properties.getLoginExpiredSeconds();
        }
        return client.getAccessTokenTtlSeconds();
    }

    private OAuthClient validateOAuthClientSecret(String clientId, String clientSecret) throws ApiException {
        if (oauthClientDao == null) {
            throw new ApiException("OAuth2 client 未配置");
        }
        OAuthClient client = oauthClientDao.getByClientIdAndStatus(clientId, OAuthClientStatus.ENABLED);
        if (client == null
                || !StringUtils.equals(Sha256Helper.hashBase64Url(clientSecret), client.getClientSecretHash())) {
            throw new ApiException("OAuth2 client secret invalid");
        }
        return client;
    }

    private OAuthClient validateOAuthClient(String clientId, String redirectUri, List<String> scopes)
            throws ApiException {
        if (oauthClientDao == null) {
            throw new ApiException("OAuth2 client 未配置");
        }
        OAuthClient client = oauthClientDao.getByClientIdAndStatus(clientId, OAuthClientStatus.ENABLED);
        Set<String> requestedScopes = toScopeSet(scopes);
        if (client == null || !client.supportsRedirectUri(redirectUri) || !client.supportsScopes(requestedScopes)) {
            throw new ApiException("OAuth2 client request invalid");
        }
        return client;
    }

    private Set<String> toScopeSet(List<String> scopes) {
        return scopes == null ? new LinkedHashSet<>() : new LinkedHashSet<>(scopes);
    }

    private boolean verifyPkce(OAuthAuthorization authorization, String codeVerifier) {
        if (StringUtils.isBlank(authorization.getCodeChallenge())) {
            return true;
        }
        if (StringUtils.isBlank(codeVerifier)) {
            return false;
        }
        if ("S256".equalsIgnoreCase(authorization.getCodeChallengeMethod())) {
            return StringUtils.equals(authorization.getCodeChallenge(), Sha256Helper.hashBase64Url(codeVerifier));
        }
        return StringUtils.equals(authorization.getCodeChallenge(), codeVerifier);
    }

    private String tokenHash(String token) {
        if (StringUtils.isBlank(token)) {
            return StringUtils.EMPTY;
        }
        return Sha256Helper.hashBase64Url(token);
    }

    private String getAccountLoginName(EntityId userId) {
        if (userId == null) {
            return null;
        }
        PrincipalIdentity identity = principalIdentityService.getByPrincipalKeyAndType(
                PrincipalKey.of(PrincipalType.USER, userId), PrincipalIdentityType.USER_ACCOUNT);
        return identity == null ? null : identity.getIdentityValue();
    }

    private PrincipalPasswordPolicyDTO passwordPolicy() {
        return new PrincipalPasswordPolicyDTO(
                loginProperties.getEnable(), loginProperties.getMaxFailCount(), loginProperties.getLockTime());
    }
}
