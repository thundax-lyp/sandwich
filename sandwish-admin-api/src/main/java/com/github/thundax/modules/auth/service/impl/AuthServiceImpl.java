package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.autoconfigure.LoginProperties;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.UuidHelper;
import com.github.thundax.common.utils.encrypt.Md5Helper;
import com.github.thundax.common.utils.encrypt.Sm2Helper;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.AccessTokenDao;
import com.github.thundax.modules.auth.dao.AuthSessionDao;
import com.github.thundax.modules.auth.dao.AuthSessionRuntimeDao;
import com.github.thundax.modules.auth.dao.LoginFormDao;
import com.github.thundax.modules.auth.dao.OAuthAuthorizationDao;
import com.github.thundax.modules.auth.dao.OAuthClientDao;
import com.github.thundax.modules.auth.dao.OAuthRefreshTokenDao;
import com.github.thundax.modules.auth.dao.UserCredentialDao;
import com.github.thundax.modules.auth.dao.UserIdentityDao;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.entity.AuthSession;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.entity.OAuthAuthorization;
import com.github.thundax.modules.auth.entity.OAuthClient;
import com.github.thundax.modules.auth.entity.OAuthRefreshToken;
import com.github.thundax.modules.auth.entity.UserCredential;
import com.github.thundax.modules.auth.entity.UserIdentity;
import com.github.thundax.modules.auth.entity.enums.AuthSessionStatus;
import com.github.thundax.modules.auth.entity.enums.OAuthClientStatus;
import com.github.thundax.modules.auth.entity.enums.OAuthRefreshTokenStatus;
import com.github.thundax.modules.auth.entity.enums.UserCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.UserCredentialType;
import com.github.thundax.modules.auth.entity.enums.UserIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.UserIdentityType;
import com.github.thundax.modules.auth.exception.BannedAccountException;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.InvalidUsernamePasswordException;
import com.github.thundax.modules.auth.exception.TooManyLoginRequestException;
import com.github.thundax.modules.auth.exception.TooManyOnlineUserException;
import com.github.thundax.modules.auth.service.AuthService;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.auth.service.PermissionService;
import com.github.thundax.modules.auth.service.provider.GithubLoginProvider;
import com.github.thundax.modules.auth.service.provider.WecomLoginProvider;
import com.github.thundax.modules.auth.service.result.AuthTokenQueryResult;
import com.github.thundax.modules.auth.service.result.AuthTokenRefreshResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationDecisionResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationViewResult;
import com.github.thundax.modules.auth.utils.AuthUtils;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.UserService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int CAPTCHA_LENGTH = 4;
    private static final int SMS_VALIDATE_CODE_LENGTH = 6;
    private static final int SESSION_RUNTIME_SAFETY_SECONDS = 10;

    private static final char[] VALIDATE_CAPTCHA_CODE = {'2', '3', '4', '5', '6', '7', '8', '9'};

    private static final char[] VALIDATE_SMS_VALIDATE_CODE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private final AuthProperties properties;
    private final LoginProperties loginProperties;
    private final LoginFormDao loginFormDao;
    private final AccessTokenDao accessTokenDao;
    private final AuthSessionDao authSessionDao;
    private final AuthSessionRuntimeDao authSessionRuntimeDao;
    private final UserIdentityDao userIdentityDao;
    private final UserCredentialDao userCredentialDao;
    private final PasswordService passwordService;
    private final PermissionService permissionService;
    private final UserService userService;

    @Autowired(required = false)
    private WecomLoginProvider wecomLoginProvider;

    @Autowired(required = false)
    private GithubLoginProvider githubLoginProvider;

    @Autowired(required = false)
    private OAuthAuthorizationDao oauthAuthorizationDao;

    @Autowired(required = false)
    private OAuthClientDao oauthClientDao;

    @Autowired(required = false)
    private OAuthRefreshTokenDao oauthRefreshTokenDao;

    public AuthServiceImpl(
            AuthProperties properties,
            LoginProperties loginProperties,
            LoginFormDao loginFormDao,
            AccessTokenDao accessTokenDao,
            AuthSessionDao authSessionDao,
            AuthSessionRuntimeDao authSessionRuntimeDao,
            UserIdentityDao userIdentityDao,
            UserCredentialDao userCredentialDao,
            PasswordService passwordService,
            PermissionService permissionService,
            UserService userService) {
        this.properties = properties;
        this.loginProperties = loginProperties;
        this.loginFormDao = loginFormDao;
        this.accessTokenDao = accessTokenDao;
        this.authSessionDao = authSessionDao;
        this.authSessionRuntimeDao = authSessionRuntimeDao;
        this.userIdentityDao = userIdentityDao;
        this.userCredentialDao = userCredentialDao;
        this.passwordService = passwordService;
        this.permissionService = permissionService;
        this.userService = userService;
    }

    @Override
    public LoginForm createLoginForm() throws TooManyLoginRequestException, TooManyOnlineUserException {
        // 检测是否登录请求过多
        if (loginFormDao.count() > properties.getMaxLoginCount()) {
            throw new TooManyLoginRequestException();
        }

        // 检测是否在线用户过多
        if (accessTokenDao.count() > properties.getMaxOnlineCount()) {
            throw new TooManyOnlineUserException();
        }

        LoginForm form = new LoginForm();
        form.setLoginToken(UuidHelper.compact());
        form.setRefreshTokenList(new ArrayList<>(Collections.singletonList(UuidHelper.compact())));
        form.setExpiredSeconds(properties.getLoginExpiredSeconds());
        form.setCheckCode(AuthUtils.currentCheckCode());
        form.setCaptcha(createCode(VALIDATE_CAPTCHA_CODE, CAPTCHA_LENGTH));

        Sm2Helper.StringKeyPair keyPair = Sm2Helper.generateKeyPair();
        if (keyPair != null) {
            form.setPublicKey(keyPair.getPublicKey());
            form.setPrivateKey(keyPair.getPrivateKey());
        }

        loginFormDao.insert(form);

        return form;
    }

    @Override
    public LoginForm refreshLoginForm(String refreshToken) throws InvalidTokenException {
        // 检测refreshToken是否有效
        LoginForm form = loginFormDao.getByRefreshToken(refreshToken);
        if (form == null || !form.validateCheckCode()) {
            throw new InvalidTokenException();
        }

        List<String> refreshTokenList = new ArrayList<>(form.getRefreshTokenList());
        refreshTokenList.add(0, UuidHelper.compact());

        form.setLoginToken(UuidHelper.compact());
        form.setRefreshTokenList(refreshTokenList);
        form.setExpiredSeconds(properties.getLoginExpiredSeconds());
        form.setCheckCode(AuthUtils.currentCheckCode());

        loginFormDao.insert(form);

        return form;
    }

    @Override
    public void deleteLoginForm(String loginToken) {
        loginFormDao.deleteByToken(loginToken);
    }

    @Override
    public String createCaptcha(String loginToken) throws InvalidTokenException {
        if (!loginFormDao.tokenExists(loginToken)) {
            throw new InvalidTokenException();
        }

        String captcha = createCode(VALIDATE_CAPTCHA_CODE, CAPTCHA_LENGTH);

        loginFormDao.updateCaptcha(loginToken, captcha);

        return captcha;
    }

    @Override
    public String getCaptcha(String loginToken) throws InvalidTokenException, InvalidCaptchaException {
        LoginForm form = loginFormDao.getByToken(loginToken);
        if (form == null || !form.validateCheckCode()) {
            throw new InvalidTokenException();
        }

        if (form.isNullCaptcha()) {
            throw new InvalidCaptchaException();
        }

        return form.getCaptcha();
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
        if (!loginFormDao.tokenExists(loginToken)) {
            throw new InvalidTokenException();
        }

        String validateCode = createCode(VALIDATE_SMS_VALIDATE_CODE, SMS_VALIDATE_CODE_LENGTH);

        loginFormDao.updateSmsValidateCode(loginToken, mobile, validateCode);
        loginFormDao.updateCaptcha(loginToken, null);

        return validateCode;
    }

    @Override
    public String getSmsValidateCode(String loginToken) throws InvalidTokenException, InvalidCaptchaException {
        LoginForm form = loginFormDao.getByToken(loginToken);
        if (form == null) {
            throw new InvalidTokenException();
        }

        if (StringUtils.isEmpty(form.getMobileValidateCode())) {
            throw new InvalidCaptchaException();
        }

        return form.getMobileValidateCode();
    }

    @Override
    public boolean validateSmsValidateCode(String loginToken, String mobile, String validateCode)
            throws InvalidTokenException, InvalidCaptchaException {
        if (StringUtils.isNotBlank(properties.getWhiteCaptcha())
                && StringUtils.equals(properties.getWhiteCaptcha(), validateCode)) {
            return true;
        }

        LoginForm form = loginFormDao.getByToken(loginToken);
        if (form == null) {
            throw new InvalidTokenException();

        } else if (StringUtils.isEmpty(form.getMobile()) || StringUtils.isEmpty(form.getMobileValidateCode())) {
            throw new InvalidCaptchaException();
        }

        return StringUtils.equals(form.getMobile(), mobile)
                && StringUtils.equals(form.getMobileValidateCode(), validateCode);
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
        return AuthTokenQueryResult.active(token, session, user);
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

        AccessToken accessToken = createAccessToken(EntityIdCodec.toValue(current.getUserId()));
        String nextRefreshToken = createOAuthRefreshToken(accessToken, clientId, current.getTenantId(), now);
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
        authorization.setUserId(EntityIdCodec.toDomain(userId));
        authorization.setRedirectUri(redirectUri);
        authorization.setScopes(toScopeSet(scopes));
        authorization.setState(state);
        authorization.setCodeChallenge(codeChallenge);
        authorization.setCodeChallengeMethod(codeChallengeMethod);
        authorization.setIssuedAt(now);
        authorization.setExpireAt(new Date(now.getTime() + 300000L));
        authorization.setCreateDate(now);
        authorization.setUpdateDate(now);
        authorization.setId(EntityIdCodec.toDomain(oauthAuthorizationDao.insert(authorization)));
        result.setAuthorizationCode(authorization.getAuthorizationCode());
        return result;
    }

    @Override
    public AuthTokenRefreshResult exchangeAuthorizationCode(String clientId, String authorizationCode)
            throws ApiException {
        if (oauthAuthorizationDao == null) {
            throw new ApiException("OAuth2 authorization 未配置");
        }
        OAuthAuthorization authorization = oauthAuthorizationDao.getByAuthorizationCode(authorizationCode);
        Date now = new Date();
        if (authorization == null
                || !authorization.canConsume(now)
                || !StringUtils.equals(clientId, authorization.getClientId())) {
            throw new InvalidTokenException();
        }
        authorization.markUsed(now);
        oauthAuthorizationDao.updateUsed(authorization);
        AccessToken accessToken = createAccessToken(EntityIdCodec.toValue(authorization.getUserId()));
        String refreshToken = oauthRefreshTokenDao == null
                ? null
                : createOAuthRefreshToken(accessToken, clientId, authorization.getTenantId(), now);
        return new AuthTokenRefreshResult(accessToken, refreshToken);
    }

    @Override
    public boolean revokeAuthorizationCode(String authorizationCode) throws ApiException {
        if (oauthAuthorizationDao == null) {
            throw new ApiException("OAuth2 authorization 未配置");
        }
        return oauthAuthorizationDao.deleteByAuthorizationCode(authorizationCode) > 0;
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
    public int invalidateSessionsByTenantId(String tenantId, String reason) {
        List<AuthSession> sessions = authSessionDao.listByTenantIdAndStatus(tenantId, AuthSessionStatus.ACTIVE);
        return invalidateAuthSessions(sessions, reason);
    }

    @Override
    public User authenticatePassword(String loginName, String plainPassword) throws ApiException {
        UserIdentity identity = getOrBootstrapAccountIdentity(loginName);
        if (identity == null) {
            throw new InvalidUsernamePasswordException();
        }
        if (!identity.isEnabled()) {
            throw new ApiException("登录方式已被禁用");
        }

        User user = userService.getById(identity.getUserId());
        if (user == null) {
            throw new InvalidUsernamePasswordException();
        }
        if (!user.isEnable()) {
            throw new BannedAccountException();
        }

        UserCredential credential = getOrBootstrapPasswordCredential(user, identity);
        if (credential == null) {
            throw new InvalidUsernamePasswordException();
        }
        validateCredential(credential, plainPassword);
        return user;
    }

    @Override
    public User authenticateSms(String loginToken, String mobile, String validateCode) throws ApiException {
        if (!validateSmsValidateCode(loginToken, mobile, validateCode)) {
            throw new InvalidCaptchaException();
        }
        return authenticateIdentity(UserIdentityType.MOBILE, mobile);
    }

    @Override
    public User authenticateWecom(String code) throws ApiException {
        if (wecomLoginProvider == null) {
            throw new ApiException("企业微信登录未配置");
        }
        return authenticateIdentity(UserIdentityType.WECOM, wecomLoginProvider.resolveIdentity(code));
    }

    @Override
    public User authenticateGithub(String code) throws ApiException {
        if (githubLoginProvider == null) {
            throw new ApiException("GitHub登录未配置");
        }
        return authenticateIdentity(UserIdentityType.GITHUB, githubLoginProvider.resolveIdentity(code));
    }

    @Override
    public void validatePassword(User user, String plainPassword) throws ApiException {
        if (user == null) {
            throw new InvalidUsernamePasswordException();
        }
        authenticatePassword(user.getLoginName(), plainPassword);
    }

    @Override
    public String getPrivateKey(String loginToken) throws InvalidTokenException {
        LoginForm form = loginFormDao.getByToken(loginToken);
        if (form == null || !form.validateCheckCode()) {
            throw new InvalidTokenException();
        }

        return form.getPrivateKey();
    }

    private String createCode(char[] validateChars, int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < length; idx++) {
            sb.append(validateChars[random.nextInt(validateChars.length)]);
        }
        return sb.toString();
    }

    private UserIdentity getOrBootstrapAccountIdentity(String loginName) {
        UserIdentity identity = userIdentityDao.getByIdentity(UserIdentityType.ACCOUNT, loginName);
        if (identity != null) {
            return identity;
        }

        User user = userService.getByLoginName(loginName);
        if (user == null || user.getId() == null || StringUtils.isBlank(user.getLoginName())) {
            return null;
        }

        identity = new UserIdentity();
        identity.setUserId(user.getId());
        identity.setIdentityType(UserIdentityType.ACCOUNT);
        identity.setIdentityValue(user.getLoginName());
        identity.setStatus(UserIdentityStatus.ENABLED);
        identity.setCreateDate(new Date());
        identity.setUpdateDate(identity.getCreateDate());
        identity.setId(EntityIdCodec.toDomain(userIdentityDao.insert(identity)));
        return identity;
    }

    private User authenticateIdentity(UserIdentityType identityType, String identityValue) throws ApiException {
        UserIdentity identity = userIdentityDao.getByIdentity(identityType, identityValue);
        if (identity == null || !identity.isEnabled()) {
            throw new InvalidUsernamePasswordException();
        }
        User user = userService.getById(identity.getUserId());
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
        UserIdentity identity = getOrBootstrapAccountIdentity(loginName);
        if (identity == null
                || !StringUtils.equals(accessToken.getUserId(), EntityIdCodec.toValue(identity.getUserId()))) {
            return;
        }

        Date now = new Date();
        AuthSession authSession = new AuthSession();
        authSession.setSessionId(UuidHelper.compact());
        authSession.setToken(accessToken.getToken());
        authSession.setUserId(identity.getUserId());
        authSession.setIdentityId(identity.getId());
        authSession.setIdentityType(identity.getIdentityType());
        authSession.setLoginType(UserCredentialType.PASSWORD.value());
        authSession.setStatus(AuthSessionStatus.ACTIVE);
        authSession.setIssuedAt(now);
        authSession.setLastAccessTime(now);
        authSession.setExpireAt(new Date(now.getTime() + properties.getLoginExpiredSeconds() * 1000L));
        authSession.setCreateDate(now);
        authSession.setUpdateDate(now);
        authSession.setId(EntityIdCodec.toDomain(authSessionDao.insert(authSession)));
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

    private String createOAuthRefreshToken(AccessToken accessToken, String clientId, String tenantId, Date issuedAt) {
        String refreshToken = UuidHelper.compact();
        OAuthRefreshToken entity = new OAuthRefreshToken();
        entity.setTokenId(UuidHelper.compact());
        entity.setTokenHash(tokenHash(refreshToken));
        entity.setAccessTokenId(accessToken.getToken());
        entity.setClientId(clientId);
        entity.setTenantId(tenantId);
        entity.setUserId(EntityIdCodec.toDomain(accessToken.getUserId()));
        entity.setIssuedAt(issuedAt);
        entity.setExpireAt(new Date(issuedAt.getTime() + refreshTokenTtlSeconds(clientId) * 1000L));
        entity.setStatus(OAuthRefreshTokenStatus.ACTIVE);
        entity.setCreateDate(issuedAt);
        entity.setUpdateDate(issuedAt);
        entity.setId(EntityIdCodec.toDomain(oauthRefreshTokenDao.insert(entity)));
        return refreshToken;
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

    private OAuthClient validateOAuthClient(String clientId, String redirectUri, List<String> scopes)
            throws ApiException {
        if (oauthClientDao == null) {
            throw new ApiException("OAuth2 client 未配置");
        }
        OAuthClient client = oauthClientDao.getByClientIdAndStatus(clientId, OAuthClientStatus.ENABLED);
        Set<String> requestedScopes = toScopeSet(scopes);
        if (client == null || !client.supportsRedirectUri(redirectUri) || !client.supportsScopes(requestedScopes)) {
            throw new InvalidTokenException();
        }
        return client;
    }

    private Set<String> toScopeSet(List<String> scopes) {
        return scopes == null ? new LinkedHashSet<>() : new LinkedHashSet<>(scopes);
    }

    private String tokenHash(String token) {
        return Md5Helper.encrypt(token);
    }

    private UserCredential getOrBootstrapPasswordCredential(User user, UserIdentity identity) {
        UserCredential credential =
                userCredentialDao.getByIdentityIdAndType(identity.getId(), UserCredentialType.PASSWORD);
        if (credential != null) {
            return credential;
        }
        // 迁移兼容：老用户首次登录时用 User.loginPass 初始化独立凭据，后续认证只读 UserCredential。
        if (StringUtils.isBlank(user.getLoginPass())) {
            return null;
        }

        credential = new UserCredential();
        credential.setUserId(user.getId());
        credential.setIdentityId(identity.getId());
        credential.setCredentialType(UserCredentialType.PASSWORD);
        credential.setCredentialValue(user.getLoginPass());
        credential.setStatus(UserCredentialStatus.ACTIVE);
        credential.setFailedCount(0);
        credential.setFailedLimit(loginProperties.getMaxFailCount());
        credential.setCreateDate(new Date());
        credential.setUpdateDate(credential.getCreateDate());
        credential.setId(EntityIdCodec.toDomain(userCredentialDao.insert(credential)));
        return credential;
    }

    private void validateCredential(UserCredential credential, String plainPassword) throws ApiException {
        Date now = new Date();
        if (credential.isLocked(now)) {
            throw new ApiException("帐号已被锁定，请等待（" + lockedExpireSeconds(credential, now) + "）秒后自动解锁!");
        }
        if (credential.isExpired(now)) {
            throw new ApiException("认证凭据已过期");
        }
        if (!credential.isActive()) {
            throw new ApiException("认证凭据不可用");
        }

        if (passwordService.validate(plainPassword, credential.getCredentialValue())) {
            credential.markVerified(now);
            userCredentialDao.updateVerifyState(credential);
            return;
        }

        if (!loginProperties.getEnable()) {
            throw new InvalidUsernamePasswordException();
        }

        if (credential.getFailedLimit() <= 0) {
            credential.setFailedLimit(loginProperties.getMaxFailCount());
        }
        Date lockedUntil = new Date(now.getTime() + loginProperties.getLockTime() * 1000L);
        credential.markFailed(lockedUntil);
        userCredentialDao.updateVerifyState(credential);
        if (credential.isLocked(now)) {
            throw new ApiException("帐号已被锁定，请等待（" + loginProperties.getLockTime() + "）秒后自动解锁!");
        }
        String message = "密码输入错误"
                + credential.getFailedLimit()
                + "次后将被锁定，剩余"
                + (credential.getFailedLimit() - credential.getFailedCount())
                + "次";
        throw new ApiException(message);
    }

    private long lockedExpireSeconds(UserCredential credential, Date now) {
        if (credential.getLockedUntil() == null) {
            return loginProperties.getLockTime();
        }
        long remaining = (credential.getLockedUntil().getTime() - now.getTime()) / 1000L;
        return Math.max(remaining, 0L);
    }
}
