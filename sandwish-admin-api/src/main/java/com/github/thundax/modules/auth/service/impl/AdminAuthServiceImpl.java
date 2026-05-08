package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.autoconfigure.LoginProperties;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.UuidHelper;
import com.github.thundax.common.utils.encrypt.Sha256Helper;
import com.github.thundax.common.utils.encrypt.Sm2Helper;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.AuthSessionDao;
import com.github.thundax.modules.auth.dao.AuthSessionRuntimeDao;
import com.github.thundax.modules.auth.dao.OAuthAuthorizationDao;
import com.github.thundax.modules.auth.dao.OAuthClientDao;
import com.github.thundax.modules.auth.dao.PrincipalAccessTokenDao;
import com.github.thundax.modules.auth.dao.PrincipalRefreshTokenDao;
import com.github.thundax.modules.auth.entity.AuthSession;
import com.github.thundax.modules.auth.entity.OAuthAuthorization;
import com.github.thundax.modules.auth.entity.OAuthClient;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.PrincipalRefreshToken;
import com.github.thundax.modules.auth.entity.enums.AuthSessionStatus;
import com.github.thundax.modules.auth.entity.enums.OAuthClientStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalTokenStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.exception.BannedAccountException;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.exception.InvalidUsernamePasswordException;
import com.github.thundax.modules.auth.exception.TooManyLoginRequestException;
import com.github.thundax.modules.auth.exception.TooManyOnlineUserException;
import com.github.thundax.modules.auth.service.AdminAuthService;
import com.github.thundax.modules.auth.service.PermissionService;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.PrincipalAuthService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.github.thundax.modules.auth.service.provider.GithubLoginProvider;
import com.github.thundax.modules.auth.service.provider.WecomLoginProvider;
import com.github.thundax.modules.auth.service.result.AuthAccessTokenResult;
import com.github.thundax.modules.auth.service.result.AuthTokenQueryResult;
import com.github.thundax.modules.auth.service.result.AuthTokenRefreshResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationDecisionResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationViewResult;
import com.github.thundax.modules.auth.utils.PreAuthCodeHelper;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.UserService;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final int SESSION_RUNTIME_SAFETY_SECONDS = 10;
    private static final int CAPTCHA_EXPIRED_SECONDS = 60;
    private static final int REFRESH_TOKEN_GRACE_SECONDS = 60;
    private static final String ADMIN_CLIENT_ID = "admin-api";
    private static final String CAPTCHA_ITEM = "CAPTCHA";
    private static final String SMS_MOBILE_ITEM = "SMS_MOBILE";
    private static final String SMS_VALIDATE_CODE_ITEM = "SMS_VALIDATE_CODE";
    private static final String PUBLIC_KEY_ITEM = "publicKey";
    private static final String PRIVATE_KEY_ITEM = "privateKey";

    private final AuthProperties properties;
    private final LoginProperties loginProperties;
    private final PreAuthSessionService preAuthSessionService;
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
    private PrincipalAccessTokenDao principalAccessTokenDao;

    @Autowired(required = false)
    private OAuthClientDao oauthClientDao;

    @Autowired(required = false)
    private PrincipalRefreshTokenDao principalRefreshTokenDao;

    public AdminAuthServiceImpl(
            AuthProperties properties,
            LoginProperties loginProperties,
            PreAuthSessionService preAuthSessionService,
            AuthSessionDao authSessionDao,
            AuthSessionRuntimeDao authSessionRuntimeDao,
            PermissionService permissionService,
            PrincipalAuthService principalAuthService,
            PrincipalIdentityService principalIdentityService,
            UserService userService) {
        this.properties = properties;
        this.loginProperties = loginProperties;
        this.preAuthSessionService = preAuthSessionService;
        this.authSessionDao = authSessionDao;
        this.authSessionRuntimeDao = authSessionRuntimeDao;
        this.permissionService = permissionService;
        this.principalAuthService = principalAuthService;
        this.principalIdentityService = principalIdentityService;
        this.userService = userService;
    }

    @Override
    public PreAuthSession createPreAuthSession() throws ApiException {
        if (preAuthSessionService.count() > properties.getMaxLoginCount()) {
            throw new TooManyLoginRequestException();
        }
        if (requirePrincipalAccessTokenDao().countByClientIdAndStatus(ADMIN_CLIENT_ID, PrincipalTokenStatus.ACTIVE)
                > properties.getMaxOnlineCount()) {
            throw new TooManyOnlineUserException();
        }
        PreAuthSession session = preAuthSessionService.create(properties.getLoginExpiredSeconds());
        writeCaptcha(session.getId(), PreAuthCodeHelper.generateCaptcha());
        Sm2Helper.StringKeyPair keyPair = Sm2Helper.generateKeyPair();
        if (keyPair != null) {
            preAuthSessionService.upsertValue(
                    session.getId(), PUBLIC_KEY_ITEM, keyPair.getPublicKey(), session.getExpiredAt());
            preAuthSessionService.upsertValue(
                    session.getId(), PRIVATE_KEY_ITEM, keyPair.getPrivateKey(), session.getExpiredAt());
        }
        return preAuthSessionService.getById(session.getId());
    }

    @Override
    public PreAuthSession refreshPreAuthSession(String refreshToken) throws ApiException {
        PreAuthSessionId sessionId = requireSessionIdByRefreshToken(refreshToken);
        PreAuthSession session = preAuthSessionService.refresh(
                sessionId, properties.getLoginExpiredSeconds(), REFRESH_TOKEN_GRACE_SECONDS);
        writeCaptcha(session.getId(), PreAuthCodeHelper.generateCaptcha());
        return session;
    }

    @Override
    public void releasePreAuthSession(String loginToken) {
        PreAuthSessionId sessionId = preAuthSessionService.findIdByToken(PreAuthSessionToken.of(loginToken));
        if (sessionId != null) {
            preAuthSessionService.release(sessionId);
        }
    }

    @Override
    public String createCaptcha(String loginToken) throws InvalidTokenException {
        String captcha = PreAuthCodeHelper.generateCaptcha();
        writeCaptcha(requireSessionIdByToken(loginToken), captcha);
        return captcha;
    }

    @Override
    public String getCaptcha(String loginToken) throws InvalidTokenException, InvalidCaptchaException {
        String captcha = preAuthSessionService.findValue(requireSessionIdByToken(loginToken), CAPTCHA_ITEM);
        if (StringUtils.isEmpty(captcha)) {
            throw new InvalidCaptchaException();
        }
        return captcha;
    }

    @Override
    public boolean validateCaptcha(String loginToken, String captcha)
            throws InvalidTokenException, InvalidCaptchaException {
        if (StringUtils.isNotBlank(properties.getWhiteCaptcha())
                && StringUtils.equals(properties.getWhiteCaptcha(), captcha)) {
            return true;
        }
        return StringUtils.equals(captcha, getCaptcha(loginToken));
    }

    @Override
    public String createSmsValidateCode(String loginToken, String mobile) throws InvalidTokenException {
        PreAuthSession session = preAuthSessionService.getById(requireSessionIdByToken(loginToken));
        String validateCode = PreAuthCodeHelper.generateSmsCode();
        preAuthSessionService.upsertValue(session.getId(), SMS_MOBILE_ITEM, mobile, session.getExpiredAt());
        preAuthSessionService.upsertValue(
                session.getId(), SMS_VALIDATE_CODE_ITEM, validateCode, session.getExpiredAt());
        preAuthSessionService.upsertValue(session.getId(), CAPTCHA_ITEM, null, 0L);
        return validateCode;
    }

    @Override
    public String getSmsValidateCode(String loginToken) throws InvalidTokenException, InvalidCaptchaException {
        String validateCode =
                preAuthSessionService.findValue(requireSessionIdByToken(loginToken), SMS_VALIDATE_CODE_ITEM);
        if (StringUtils.isEmpty(validateCode)) {
            throw new InvalidCaptchaException();
        }
        return validateCode;
    }

    @Override
    public boolean validateSmsValidateCode(String loginToken, String mobile, String validateCode)
            throws InvalidTokenException, InvalidCaptchaException {
        if (StringUtils.isNotBlank(properties.getWhiteCaptcha())
                && StringUtils.equals(properties.getWhiteCaptcha(), validateCode)) {
            return true;
        }
        PreAuthSessionId sessionId = requireSessionIdByToken(loginToken);
        String savedMobile = preAuthSessionService.findValue(sessionId, SMS_MOBILE_ITEM);
        String savedValidateCode = preAuthSessionService.findValue(sessionId, SMS_VALIDATE_CODE_ITEM);
        if (StringUtils.isEmpty(savedMobile) || StringUtils.isEmpty(savedValidateCode)) {
            throw new InvalidCaptchaException();
        }
        return StringUtils.equals(savedMobile, mobile) && StringUtils.equals(savedValidateCode, validateCode);
    }

    @Override
    @NonNull
    public AuthAccessTokenResult createAccessToken(String userId) {
        return createAccessToken(userId, null);
    }

    @Override
    @NonNull
    public AuthAccessTokenResult createAccessToken(String userId, String loginName) {
        Date now = new Date();
        String token = UuidHelper.compact();
        PrincipalAccessToken accessToken = buildPrincipalAccessToken(
                token,
                ADMIN_CLIENT_ID,
                PrincipalKey.of(PrincipalType.USER, EntityIdCodec.toDomain(Long.valueOf(userId))),
                new LinkedHashSet<>(),
                now,
                properties.getLoginExpiredSeconds());
        AuthSession authSession = createAuthSession(token, userId, loginName, now);
        if (authSession != null) {
            accessToken.setSessionId(authSession.getSessionId());
        }
        accessToken.setId(requirePrincipalAccessTokenDao().insert(accessToken));
        permissionService.createSession(token, userId);
        String refreshToken = createPrincipalRefreshToken(accessToken, ADMIN_CLIENT_ID, now);
        return new AuthAccessTokenResult(token, refreshToken, accessToken);
    }

    @Override
    public AuthAccessTokenResult getAccessToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        PrincipalAccessToken accessToken = requirePrincipalAccessTokenDao().getByTokenHash(tokenHash(token));
        if (accessToken == null
                || !StringUtils.equals(ADMIN_CLIENT_ID, accessToken.getClientId())
                || !accessToken.canAccess(new Date())) {
            return null;
        }
        return new AuthAccessTokenResult(token, null, accessToken);
    }

    @Override
    public int deleteAccessTokensByUserId(String userId) {
        int count = invalidateAuthSessions(
                authSessionDao.listByUserIdAndStatus(
                        EntityIdCodec.toDomain(Long.valueOf(userId)), AuthSessionStatus.ACTIVE),
                "RELOGIN");
        List<PrincipalAccessToken> tokens = requirePrincipalAccessTokenDao()
                .listByPrincipalKeyAndClientIdAndStatus(
                        PrincipalKey.of(PrincipalType.USER, EntityIdCodec.toDomain(Long.valueOf(userId))),
                        ADMIN_CLIENT_ID,
                        PrincipalTokenStatus.ACTIVE);
        Date now = new Date();
        for (PrincipalAccessToken token : tokens) {
            if (token != null && token.isActive()) {
                token.revoke(now);
                requirePrincipalAccessTokenDao().updateStatus(token);
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean validateToken(AuthAccessTokenResult accessToken) {
        return accessToken != null
                && accessToken.getPrincipalAccessToken() != null
                && accessToken.getPrincipalAccessToken().canAccess(new Date());
    }

    @Override
    public void activeAccessToken(AuthAccessTokenResult accessToken) {
        permissionService.touch(accessToken.getToken());
        touchAuthSession(accessToken.getToken());
    }

    @Override
    public void deleteAccessToken(AuthAccessTokenResult accessToken) {
        if (accessToken == null) {
            return;
        }
        PrincipalAccessToken principalAccessToken = accessToken.getPrincipalAccessToken();
        if (principalAccessToken != null && principalAccessToken.isActive()) {
            principalAccessToken.revoke(new Date());
            requirePrincipalAccessTokenDao().updateStatus(principalAccessToken);
        }
        permissionService.release(accessToken.getToken());
        logoutAuthSession(accessToken.getToken());
    }

    @Override
    public AuthTokenQueryResult queryToken(String token) {
        AuthTokenQueryResult oauthResult = queryOAuthAccessToken(token);
        if (oauthResult != null) {
            return oauthResult;
        }
        AuthAccessTokenResult accessToken = getAccessToken(token);
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
        if (principalAccessTokenDao == null) {
            return null;
        }
        PrincipalAccessToken accessToken = principalAccessTokenDao.getByTokenHash(tokenHash(token));
        if (accessToken == null) {
            return null;
        }
        if (StringUtils.equals(ADMIN_CLIENT_ID, accessToken.getClientId())) {
            return null;
        }
        if (!accessToken.canAccess(new Date())) {
            return AuthTokenQueryResult.inactive(token);
        }
        User user = userService.getById(accessToken.getPrincipalKey().getPrincipalId());
        if (user == null || !user.isEnable()) {
            return AuthTokenQueryResult.inactive(token);
        }
        return AuthTokenQueryResult.active(token, accessToken, user, getAccountLoginName(user.getId()));
    }

    @Override
    public AuthTokenRefreshResult refreshAccessToken(String clientId, String refreshToken) throws ApiException {
        if (principalRefreshTokenDao == null) {
            throw new ApiException("refresh token 未配置");
        }
        String requestedClientId = StringUtils.defaultIfBlank(clientId, ADMIN_CLIENT_ID);
        PrincipalRefreshToken current = principalRefreshTokenDao.getByTokenHash(tokenHash(refreshToken));
        Date now = new Date();
        if (current == null
                || !current.canRefresh(now)
                || !StringUtils.equals(requestedClientId, current.getClientId())) {
            throw new InvalidTokenException();
        }
        current.markUsed(now);
        principalRefreshTokenDao.updateStatus(current);

        AuthAccessTokenResult accessToken = createAccessToken(
                EntityIdCodec.toStringValue(current.getPrincipalKey().getPrincipalId()));
        return new AuthTokenRefreshResult(accessToken, accessToken.getRefreshToken());
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
        AuthAccessTokenResult oauthAccessToken = createOAuthAccessToken(client, authorization, now);
        String refreshToken = principalRefreshTokenDao == null
                ? null
                : createPrincipalRefreshToken(oauthAccessToken.getPrincipalAccessToken(), client.getClientId(), now);
        return new AuthTokenRefreshResult(oauthAccessToken, refreshToken, oauthAccessToken.getToken());
    }

    private AuthTokenRefreshResult refreshOAuth2Token(OAuthClient client, String refreshToken) throws ApiException {
        if (principalRefreshTokenDao == null) {
            throw new ApiException("refresh token 未配置");
        }
        PrincipalRefreshToken current = principalRefreshTokenDao.getByTokenHash(tokenHash(refreshToken));
        Date now = new Date();
        if (current == null
                || !current.canRefresh(now)
                || !StringUtils.equals(client.getClientId(), current.getClientId())) {
            throw new InvalidTokenException();
        }
        current.markUsed(now);
        principalRefreshTokenDao.updateStatus(current);

        AuthAccessTokenResult oauthAccessToken = createOAuthAccessToken(client, current, now);
        String nextRefreshToken =
                createPrincipalRefreshToken(oauthAccessToken.getPrincipalAccessToken(), client.getClientId(), now);
        return new AuthTokenRefreshResult(oauthAccessToken, nextRefreshToken, oauthAccessToken.getToken());
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
        if (principalAccessTokenDao != null) {
            PrincipalAccessToken accessToken = principalAccessTokenDao.getByTokenHash(tokenHash(token));
            if (accessToken != null && accessToken.isActive()) {
                accessToken.revoke(now);
                principalAccessTokenDao.updateStatus(accessToken);
                revoked = true;
            }
        }
        if (principalRefreshTokenDao != null) {
            PrincipalRefreshToken refreshToken = principalRefreshTokenDao.getByTokenHash(tokenHash(token));
            if (refreshToken != null && refreshToken.isActive()) {
                refreshToken.revoke(now);
                principalRefreshTokenDao.updateStatus(refreshToken);
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
        String privateKey = preAuthSessionService.findValue(requireSessionIdByToken(loginToken), PRIVATE_KEY_ITEM);
        if (StringUtils.isBlank(privateKey)) {
            throw new InvalidTokenException();
        }
        return privateKey;
    }

    private void writeCaptcha(PreAuthSessionId sessionId, String captcha) throws InvalidTokenException {
        preAuthSessionService.upsertValue(
                sessionId, CAPTCHA_ITEM, captcha, System.currentTimeMillis() + CAPTCHA_EXPIRED_SECONDS * 1000L);
    }

    private PreAuthSessionId requireSessionIdByToken(String token) throws InvalidTokenException {
        PreAuthSessionId sessionId = preAuthSessionService.findIdByToken(PreAuthSessionToken.of(token));
        if (sessionId == null) {
            throw new InvalidTokenException();
        }
        return sessionId;
    }

    private PreAuthSessionId requireSessionIdByRefreshToken(String refreshToken) throws InvalidTokenException {
        PreAuthSessionId sessionId = preAuthSessionService.findIdByRefreshToken(PreAuthSessionToken.of(refreshToken));
        if (sessionId == null) {
            throw new InvalidTokenException();
        }
        return sessionId;
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

    private AuthSession createAuthSession(String token, String userId, String loginName, Date now) {
        if (StringUtils.isBlank(loginName)) {
            return null;
        }
        PrincipalIdentity identity =
                principalIdentityService.getByIdentity(PrincipalIdentityType.USER_ACCOUNT, loginName);
        if (identity == null
                || identity.getPrincipalKey() == null
                || !StringUtils.equals(
                        userId,
                        EntityIdCodec.toStringValue(identity.getPrincipalKey().getPrincipalId()))) {
            return null;
        }

        AuthSession authSession = new AuthSession();
        authSession.setSessionId(UuidHelper.compact());
        authSession.setToken(token);
        authSession.setUserId(identity.getPrincipalKey().getPrincipalId());
        authSession.setIdentityId(identity.getId());
        authSession.setIdentityType(identity.getType());
        authSession.setLoginType(PrincipalCredentialType.USER_PASSWORD.credentialName());
        authSession.setStatus(AuthSessionStatus.ACTIVE);
        authSession.setIssuedAt(now);
        authSession.setLastAccessTime(now);
        authSession.setExpireAt(new Date(now.getTime() + properties.getLoginExpiredSeconds() * 1000L));
        authSession.setId(authSessionDao.insert(authSession));
        authSessionRuntimeDao.insert(authSession, runtimeExpiredSeconds());
        return authSession;
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
        AuthAccessTokenResult accessToken = getAccessToken(token);
        if (accessToken != null && accessToken.getPrincipalAccessToken() != null) {
            PrincipalAccessToken principalAccessToken = accessToken.getPrincipalAccessToken();
            principalAccessToken.revoke(new Date());
            requirePrincipalAccessTokenDao().updateStatus(principalAccessToken);
        }
        authSessionRuntimeDao.deleteByToken(token);
    }

    private int runtimeExpiredSeconds() {
        return properties.getLoginExpiredSeconds() + SESSION_RUNTIME_SAFETY_SECONDS;
    }

    private String createPrincipalRefreshToken(PrincipalAccessToken accessToken, String clientId, Date issuedAt) {
        String refreshToken = UuidHelper.compact();
        PrincipalRefreshToken entity = new PrincipalRefreshToken();
        entity.setTokenId(UuidHelper.compact());
        entity.setTokenHash(tokenHash(refreshToken));
        entity.setAccessTokenId(accessToken.getTokenId());
        entity.setClientId(clientId);
        entity.setSessionId(accessToken.getSessionId());
        entity.setPrincipalKey(accessToken.getPrincipalKey());
        entity.setIssuedAt(issuedAt);
        entity.setExpireAt(new Date(issuedAt.getTime() + refreshTokenTtlSeconds(clientId) * 1000L));
        entity.setStatus(PrincipalTokenStatus.ACTIVE);
        entity.setId(requirePrincipalRefreshTokenDao().insert(entity));
        return refreshToken;
    }

    private AuthAccessTokenResult createOAuthAccessToken(
            OAuthClient client, OAuthAuthorization authorization, Date issuedAt) {
        String token = UuidHelper.compact();
        PrincipalAccessToken entity = buildPrincipalAccessToken(
                token,
                client.getClientId(),
                PrincipalKey.of(PrincipalType.USER, authorization.getUserId()),
                authorization.getScopes(),
                issuedAt,
                accessTokenTtlSeconds(client));
        entity.setId(requirePrincipalAccessTokenDao().insert(entity));
        return new AuthAccessTokenResult(token, null, entity);
    }

    private AuthAccessTokenResult createOAuthAccessToken(
            OAuthClient client, PrincipalRefreshToken refreshToken, Date issuedAt) {
        String token = UuidHelper.compact();
        PrincipalAccessToken entity = buildPrincipalAccessToken(
                token,
                client.getClientId(),
                refreshToken.getPrincipalKey(),
                new LinkedHashSet<>(),
                issuedAt,
                accessTokenTtlSeconds(client));
        entity.setId(requirePrincipalAccessTokenDao().insert(entity));
        return new AuthAccessTokenResult(token, null, entity);
    }

    private PrincipalAccessToken buildPrincipalAccessToken(
            String token,
            String clientId,
            PrincipalKey principalKey,
            Set<String> scopes,
            Date issuedAt,
            long ttlSeconds) {
        PrincipalAccessToken entity = new PrincipalAccessToken();
        entity.setTokenId(UuidHelper.compact());
        entity.setTokenHash(tokenHash(token));
        entity.setClientId(clientId);
        entity.setPrincipalKey(principalKey);
        entity.setScopes(scopes == null ? new LinkedHashSet<>() : new LinkedHashSet<>(scopes));
        entity.setIssuedAt(issuedAt);
        entity.setExpireAt(new Date(issuedAt.getTime() + ttlSeconds * 1000L));
        entity.setStatus(PrincipalTokenStatus.ACTIVE);
        return entity;
    }

    private PrincipalAccessTokenDao requirePrincipalAccessTokenDao() {
        if (principalAccessTokenDao == null) {
            throw new IllegalStateException("principal access token dao 未配置");
        }
        return principalAccessTokenDao;
    }

    private PrincipalRefreshTokenDao requirePrincipalRefreshTokenDao() {
        if (principalRefreshTokenDao == null) {
            throw new IllegalStateException("principal refresh token dao 未配置");
        }
        return principalRefreshTokenDao;
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
