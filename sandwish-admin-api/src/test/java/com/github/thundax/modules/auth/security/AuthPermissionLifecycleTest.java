package com.github.thundax.modules.auth.security;

import com.github.thundax.autoconfigure.LoginProperties;
import com.github.thundax.autoconfigure.SandwishProperties;
import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.utils.encrypt.Sha256Helper;
import com.github.thundax.modules.auth.assembler.AuthInterfaceAssembler;
import com.github.thundax.modules.auth.codec.PrincipalAccessTokenIdCodec;
import com.github.thundax.modules.auth.codec.PrincipalRefreshTokenIdCodec;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.controller.response.OAuth2IntrospectionResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2UserinfoResponse;
import com.github.thundax.modules.auth.dao.*;
import com.github.thundax.modules.auth.entity.*;
import com.github.thundax.modules.auth.entity.enums.*;
import com.github.thundax.modules.auth.entity.valueobject.*;
import com.github.thundax.modules.auth.security.filter.AccessTokenAuthenticationFilter;
import com.github.thundax.modules.auth.service.*;
import com.github.thundax.modules.auth.service.command.*;
import com.github.thundax.modules.auth.service.impl.AdminAuthServiceImpl;
import com.github.thundax.modules.auth.service.impl.PermissionServiceImpl;
import com.github.thundax.modules.auth.service.provider.GithubLoginProvider;
import com.github.thundax.modules.auth.service.provider.WecomLoginProvider;
import com.github.thundax.modules.auth.service.query.AdminAuthQuery;
import com.github.thundax.modules.auth.service.query.PrincipalCredentialQuery;
import com.github.thundax.modules.auth.service.query.PrincipalIdentityQuery;
import com.github.thundax.modules.auth.service.result.*;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.entity.valueobject.MenuId;
import com.github.thundax.modules.sys.entity.valueobject.MenuIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.RoleId;
import com.github.thundax.modules.sys.entity.valueobject.RoleIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.UserId;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.command.*;
import com.github.thundax.modules.sys.service.impl.CurrentUserServiceImpl;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthPermissionLifecycleTest {

    private TestPrincipalAccessTokenDao accessTokenDao;
    private TestPrincipalRefreshTokenDao refreshTokenDao;
    private TestPrincipalAuthSessionDao principalAuthSessionDao;
    private AdminAuthService authService;
    private PermissionService permissionService;

    @Before
    public void setUp() throws Exception {
        accessTokenDao = new TestPrincipalAccessTokenDao();
        refreshTokenDao = new TestPrincipalRefreshTokenDao();
        principalAuthSessionDao = new TestPrincipalAuthSessionDao();

        AuthProperties authProperties = new AuthProperties();
        authProperties.setLoginExpiredSeconds(60);
        TestUserService userService = new TestUserService();
        TestPrincipalIdentityService principalIdentityService = new TestPrincipalIdentityService();
        TestPrincipalCredentialService principalCredentialService = new TestPrincipalCredentialService();
        permissionService = new PermissionServiceImpl(
                accessTokenDao,
                principalAuthSessionDao,
                userService,
                new CurrentUserServiceImpl(
                        userService,
                        new TestRoleService(),
                        new TestMenuService(),
                        principalIdentityService,
                        principalCredentialService));
        authService = new AdminAuthServiceImpl(
                authProperties,
                new LoginProperties(),
                principalAuthSessionDao,
                permissionService,
                new TestPrincipalAuthService(),
                principalIdentityService,
                userService);
        inject(authService, "principalAccessTokenDao", accessTokenDao);
        inject(authService, "principalRefreshTokenDao", refreshTokenDao);
    }

    @After
    public void tearDown() {
        UserAccessHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldCreateTouchAndReleasePermissionValuesWithAccessToken() {
        AuthAccessTokenResult accessToken = createAccessToken("1", "tester");

        Assert.assertNotNull(permissionService.getPermissions(accessToken.getToken()));
        Assert.assertNotNull(principalAuthSessionDao.getById(
                accessToken.getPrincipalAccessToken().getSessionId()));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "sys:role:view"));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "user"));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "admin"));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "super"));
        Date databaseLastAccessTime = principalAuthSessionDao
                .getById(accessToken.getPrincipalAccessToken().getSessionId())
                .getLastAccessTime();

        authService.activeAccessToken(accessTokenCommand(accessToken));
        Assert.assertTrue(principalAuthSessionDao.getTouchCount() > 0);
        Assert.assertTrue(principalAuthSessionDao
                        .getById(accessToken.getPrincipalAccessToken().getSessionId())
                        .getLastAccessTime()
                        .getTime()
                >= databaseLastAccessTime.getTime());

        authService.deleteAccessToken(accessTokenCommand(accessToken));
        Assert.assertNull(permissionService.getPermissions(accessToken.getToken()));
        Assert.assertNull(principalAuthSessionDao.getById(
                accessToken.getPrincipalAccessToken().getSessionId()));
    }

    @Test
    public void shouldInvalidateSessionByUserId() {
        AuthAccessTokenResult accessToken = createAccessToken("1", "tester");

        authService.invalidateSessionsByUserId(userSessionCommand(1L, "PASSWORD_RESET"));

        Assert.assertEquals(
                PrincipalTokenStatus.REVOKED,
                accessToken.getPrincipalAccessToken().getStatus());
        Assert.assertNull(principalAuthSessionDao.getById(
                accessToken.getPrincipalAccessToken().getSessionId()));
    }

    @Test
    public void shouldQueryTokenActiveStateAndUserinfo() {
        AuthAccessTokenResult accessToken = createAccessToken("1", "tester");

        AuthTokenQueryResult result = authService.getTokenInfo(tokenQuery(accessToken.getToken()));

        Assert.assertTrue(result.isActive());
        Assert.assertEquals(
                accessToken.getPrincipalAccessToken().getSessionId(),
                result.getSession().getId());
        Assert.assertEquals("tester", result.getUsername());
        Assert.assertFalse(authService.getTokenInfo(tokenQuery("missing")).isActive());
    }

    @Test
    public void shouldRefreshAccessTokenAndRotateRefreshToken() throws Exception {
        TestPrincipalRefreshTokenDao refreshTokenDao = new TestPrincipalRefreshTokenDao();
        inject(authService, "principalRefreshTokenDao", refreshTokenDao);
        inject(authService, "oauthClientDao", new TestOAuthClientDao());

        PrincipalRefreshToken refreshToken = new PrincipalRefreshToken();
        refreshToken.setId(PrincipalRefreshTokenIdCodec.toDomain("2001"));
        refreshToken.setTokenCode(PrincipalRefreshTokenCode.of("refresh-token-1"));
        refreshToken.setAccessTokenId(PrincipalAccessTokenId.of("old-access-token"));
        refreshToken.setClientId("admin-web");
        refreshToken.setSessionId(PrincipalAuthSessionId.of("oauth-session-1"));
        refreshToken.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, 1L));
        refreshToken.setIssuedAt(new Date(1000L));
        refreshToken.setExpireAt(new Date(System.currentTimeMillis() + 60000L));
        refreshToken.setStatus(PrincipalTokenStatus.ACTIVE);
        refreshTokenDao.current = refreshToken;
        refreshTokenDao.currentToken = "plain-refresh-token";
        principalAuthSessionDao.insert(principalAuthSession(refreshToken.getSessionId(), "admin-web"), 60);

        AuthTokenRefreshResult result =
                authService.refreshAccessToken(refreshTokenCommand("admin-web", "plain-refresh-token"));

        Assert.assertNotNull(result.getAccessToken().getToken());
        Assert.assertNotNull(result.getRefreshToken());
        Assert.assertEquals(PrincipalTokenStatus.USED, refreshToken.getStatus());
        Assert.assertEquals(PrincipalTokenStatus.ACTIVE, refreshTokenDao.inserted.getStatus());
    }

    @Test
    public void shouldAuthorizeApproveExchangeAndRevokeAuthorizationCode() throws Exception {
        TestOAuthAuthorizationDao authorizationDao = new TestOAuthAuthorizationDao();
        TestPrincipalRefreshTokenDao refreshTokenDao = new TestPrincipalRefreshTokenDao();
        TestPrincipalAccessTokenDao accessTokenDao = new TestPrincipalAccessTokenDao();
        inject(authService, "oauthAuthorizationDao", authorizationDao);
        inject(authService, "principalAccessTokenDao", accessTokenDao);
        inject(authService, "principalRefreshTokenDao", refreshTokenDao);
        inject(authService, "oauthClientDao", new TestOAuthClientDao());

        OAuth2AuthorizationViewResult view = authService.authorizeOAuth2(
                oauthCommand("admin-web", "http://127.0.0.1/callback", Arrays.asList("openid", "profile"), "state-1"));

        Assert.assertEquals("admin-web", view.getClientId());
        Assert.assertEquals("Admin Web", view.getClientName());
        Assert.assertTrue(view.getScopes().contains("openid"));

        String codeVerifier = "plain-verifier";
        String codeChallenge = Sha256Helper.hashBase64Url(codeVerifier);
        OAuth2AuthorizationDecisionResult decision = authService.decideOAuth2(decisionCommand(
                "admin-web",
                "http://127.0.0.1/callback",
                Arrays.asList("openid", "profile"),
                "state-1",
                codeChallenge,
                "S256",
                "1",
                true));

        Assert.assertTrue(decision.isApproved());
        Assert.assertNotNull(decision.getAuthorizationCode());
        Assert.assertEquals("state-1", decision.getState());
        Assert.assertFalse(authorizationDao.current.isUsed());

        AuthTokenRefreshResult token = authService.exchangeOAuth2Token(exchangeCommand(
                "admin-web",
                "secret",
                "authorization_code",
                "http://127.0.0.1/callback",
                decision.getAuthorizationCode(),
                codeVerifier,
                null));

        Assert.assertNotNull(token.getAccessToken().getToken());
        Assert.assertNotNull(token.getRefreshToken());
        Assert.assertNotNull(token.getOauthAccessToken());
        Assert.assertTrue(authorizationDao.current.isUsed());
        Assert.assertEquals(PrincipalTokenStatus.ACTIVE, accessTokenDao.inserted.getStatus());
        Assert.assertEquals(PrincipalTokenStatus.ACTIVE, refreshTokenDao.inserted.getStatus());
        AuthTokenQueryResult queryResult = authService.getTokenInfo(tokenQuery(token.getOauthAccessToken()));
        Assert.assertTrue(queryResult.isActive());
        OAuth2IntrospectionResponse introspection = AuthInterfaceAssembler.toIntrospectionResponse(queryResult);
        Assert.assertEquals("admin-web", introspection.getClientId());
        Assert.assertEquals("openid profile", introspection.getScope());
        Assert.assertEquals("Bearer", introspection.getTokenType());
        Assert.assertTrue(introspection.getExpiresAt() > 0L);
        OAuth2UserinfoResponse userinfo = AuthInterfaceAssembler.toUserinfoResponse(queryResult);
        Assert.assertEquals("tester", userinfo.getPreferredUsername());
        Assert.assertTrue(
                authService.revokeOAuth2Token(revokeCommand("admin-web", "secret", token.getOauthAccessToken())));
        Assert.assertFalse(authService
                .getTokenInfo(tokenQuery(token.getOauthAccessToken()))
                .isActive());
        Assert.assertTrue(
                authService.revokeAuthorizationCode(authorizationCodeCommand(decision.getAuthorizationCode())));
    }

    @Test
    public void shouldExchangeRefreshTokenGrantAndRotateOAuthTokens() throws Exception {
        TestPrincipalAccessTokenDao accessTokenDao = new TestPrincipalAccessTokenDao();
        TestPrincipalRefreshTokenDao refreshTokenDao = new TestPrincipalRefreshTokenDao();
        inject(authService, "principalAccessTokenDao", accessTokenDao);
        inject(authService, "principalRefreshTokenDao", refreshTokenDao);
        inject(authService, "oauthClientDao", new TestOAuthClientDao());

        PrincipalRefreshToken refreshToken = new PrincipalRefreshToken();
        refreshToken.setId(PrincipalRefreshTokenIdCodec.toDomain("2001"));
        refreshToken.setTokenCode(PrincipalRefreshTokenCode.of("refresh-token-1"));
        refreshToken.setAccessTokenId(PrincipalAccessTokenId.of("old-access-token"));
        refreshToken.setClientId("admin-web");
        refreshToken.setSessionId(PrincipalAuthSessionId.of("oauth-session-2"));
        refreshToken.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, 1L));
        refreshToken.setIssuedAt(new Date(1000L));
        refreshToken.setExpireAt(new Date(System.currentTimeMillis() + 60000L));
        refreshToken.setStatus(PrincipalTokenStatus.ACTIVE);
        refreshTokenDao.current = refreshToken;
        refreshTokenDao.currentToken = "plain-refresh-token";
        principalAuthSessionDao.insert(principalAuthSession(refreshToken.getSessionId(), "admin-web"), 60);

        AuthTokenRefreshResult result = authService.exchangeOAuth2Token(
                exchangeCommand("admin-web", "secret", "refresh_token", null, null, null, "plain-refresh-token"));

        Assert.assertEquals(PrincipalTokenStatus.USED, refreshToken.getStatus());
        Assert.assertNotNull(result.getOauthAccessToken());
        Assert.assertEquals(PrincipalTokenStatus.ACTIVE, accessTokenDao.inserted.getStatus());
        Assert.assertEquals(PrincipalTokenStatus.ACTIVE, refreshTokenDao.inserted.getStatus());
    }

    @Test
    public void shouldRejectAuthorizationWhenClientScopeIsInvalid() throws Exception {
        inject(authService, "oauthClientDao", new TestOAuthClientDao());

        try {
            authService.authorizeOAuth2(oauthCommand(
                    "admin-web", "http://127.0.0.1/callback", Collections.singletonList("admin.write"), "state-1"));
            Assert.fail("invalid scope must be rejected");
        } catch (ApiException expected) {
            Assert.assertNotNull(expected);
        }
    }

    @Test
    public void shouldAuthenticateSmsWecomAndGithubIdentity() throws Exception {
        Assert.assertEquals(
                Long.valueOf(1L),
                UserIdCodec.toValue(authService
                        .authenticateSms(mobileCommand("13800000000"))
                        .getId()));

        inject(authService, "wecomLoginProvider", (WecomLoginProvider) code -> "wecom-user-1");
        inject(authService, "githubLoginProvider", (GithubLoginProvider) code -> "github-user-1");

        Assert.assertEquals(
                Long.valueOf(1L),
                UserIdCodec.toValue(
                        authService.authenticateWecom(codeCommand("wecom-code")).getId()));
        Assert.assertEquals(
                Long.valueOf(1L),
                UserIdCodec.toValue(authService
                        .authenticateGithub(codeCommand("github-code"))
                        .getId()));
    }

    @Test
    public void shouldAuthenticateRequestAndPopulateSpringSecurityContext() throws Exception {
        AuthAccessTokenResult accessToken = createAccessToken("1", "tester");
        AccessTokenAuthenticationFilter filter = new AccessTokenAuthenticationFilter(
                new SandwishProperties.AccessTokenFilterProperties(),
                authService,
                permissionService,
                new TestUserService());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sys/user");
        request.addHeader(Constants.HEADER_TOKEN, accessToken.getToken());
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Assert.assertEquals(
                "1", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Assert.assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        Assert.assertEquals(
                PrincipalTokenStatus.ACTIVE,
                accessToken.getPrincipalAccessToken().getStatus());
        Assert.assertTrue(principalAuthSessionDao.getTouchCount() > 0);
    }

    @Test
    public void shouldClearUserAccessHolderAfterAuthenticatedRequest() throws Exception {
        AuthAccessTokenResult accessToken = createAccessToken("1", "tester");
        AccessTokenAuthenticationFilter filter = new AccessTokenAuthenticationFilter(
                new SandwishProperties.AccessTokenFilterProperties(),
                authService,
                permissionService,
                new TestUserService());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sys/user");
        request.addHeader(Constants.HEADER_TOKEN, accessToken.getToken());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            Assert.assertEquals(Long.valueOf(1L), UserAccessHolder.currentUserId());
            Assert.assertEquals(accessToken.getToken(), UserAccessHolder.currentToken());
        });

        Assert.assertNull(UserAccessHolder.currentUserId());
        Assert.assertNull(UserAccessHolder.currentToken());
    }

    @Test
    public void shouldRejectRequestWithoutToken() throws Exception {
        AccessTokenAuthenticationFilter filter = new AccessTokenAuthenticationFilter(
                new SandwishProperties.AccessTokenFilterProperties(),
                authService,
                permissionService,
                new TestUserService());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sys/user");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/json;charset=UTF-8", response.getContentType());
        Assert.assertTrue(response.getContentAsString().contains("未授权用户"));
        Assert.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void shouldSkipConfiguredPublicAuthPathWithoutToken() throws Exception {
        SandwishProperties.AccessTokenFilterProperties properties =
                new SandwishProperties.AccessTokenFilterProperties();
        properties.setExcludePath(Collections.singletonList("/api/auth/**"));
        AccessTokenAuthenticationFilter filter =
                new AccessTokenAuthenticationFilter(properties, authService, permissionService, new TestUserService());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/captcha");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Assert.assertSame(request, chain.getRequest());
        Assert.assertEquals("", response.getContentAsString());
        Assert.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private static class TestPrincipalAuthService implements PrincipalAuthService {

        @Override
        public PrincipalIdentity authenticateIdentity(AuthenticateIdentityCommand command) {
            return identity(command.getIdentityType(), command.getIdentityValue());
        }

        @Override
        public PrincipalIdentity authenticatePassword(AuthenticatePasswordCommand command) {
            return identity(command.getIdentityType(), command.getIdentityValue());
        }

        private PrincipalIdentity identity(PrincipalIdentityType identityType, String identityValue) {
            PrincipalIdentity identity = new PrincipalIdentity();
            identity.setId(EntityIdCodec.toDomain(1001L));
            identity.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, 1L));
            identity.setType(identityType);
            identity.setIdentityValue(identityValue);
            return identity;
        }
    }

    private void inject(AdminAuthService target, String fieldName, Object value) throws Exception {
        Field field = AdminAuthServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private AuthAccessTokenResult createAccessToken(String userId, String loginName) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setUserId(userId);
        command.setLoginName(loginName);
        return authService.createAccessToken(command);
    }

    private AdminAuthCommand accessTokenCommand(AuthAccessTokenResult accessToken) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setAccessToken(accessToken);
        return command;
    }

    private AdminAuthCommand userSessionCommand(Long userId, String reason) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setEntityUserId(EntityId.of(userId));
        command.setReason(reason);
        return command;
    }

    private AdminAuthQuery tokenQuery(String token) {
        AdminAuthQuery query = new AdminAuthQuery();
        query.setToken(token);
        return query;
    }

    private AdminAuthCommand refreshTokenCommand(String clientId, String refreshToken) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setClientId(clientId);
        command.setRefreshToken(refreshToken);
        return command;
    }

    private AdminAuthCommand oauthCommand(String clientId, String redirectUri, List<String> scopes, String state) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setClientId(clientId);
        command.setRedirectUri(redirectUri);
        command.setScopes(scopes);
        command.setState(state);
        return command;
    }

    private AdminAuthCommand decisionCommand(
            String clientId,
            String redirectUri,
            List<String> scopes,
            String state,
            String codeChallenge,
            String codeChallengeMethod,
            String userId,
            boolean approved) {
        AdminAuthCommand command = oauthCommand(clientId, redirectUri, scopes, state);
        command.setCodeChallenge(codeChallenge);
        command.setCodeChallengeMethod(codeChallengeMethod);
        command.setUserId(userId);
        command.setApproved(approved);
        return command;
    }

    private AdminAuthCommand exchangeCommand(
            String clientId,
            String clientSecret,
            String grantType,
            String redirectUri,
            String authorizationCode,
            String codeVerifier,
            String refreshToken) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setClientId(clientId);
        command.setClientSecret(clientSecret);
        command.setGrantType(grantType);
        command.setRedirectUri(redirectUri);
        command.setAuthorizationCode(authorizationCode);
        command.setCodeVerifier(codeVerifier);
        command.setRefreshToken(refreshToken);
        return command;
    }

    private AdminAuthCommand revokeCommand(String clientId, String clientSecret, String token) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setClientId(clientId);
        command.setClientSecret(clientSecret);
        command.setToken(token);
        return command;
    }

    private AdminAuthCommand authorizationCodeCommand(String authorizationCode) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setAuthorizationCode(authorizationCode);
        return command;
    }

    private AdminAuthCommand mobileCommand(String mobile) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setMobile(mobile);
        return command;
    }

    private AdminAuthCommand codeCommand(String code) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setCode(code);
        return command;
    }

    private static class TestOAuthClientDao implements OAuthClientDao {

        @Override
        public OAuthClient getById(EntityId id) {
            return client();
        }

        @Override
        public OAuthClient getByClientId(String clientId) {
            return client();
        }

        @Override
        public OAuthClient getByClientIdAndStatus(String clientId, OAuthClientStatus status) {
            return client();
        }

        @Override
        public EntityId insert(OAuthClient client) {
            return EntityId.of(3001L);
        }

        @Override
        public int update(OAuthClient client) {
            return 1;
        }

        private OAuthClient client() {
            OAuthClient client = new OAuthClient();
            client.setId(EntityIdCodec.toDomain(3001L));
            client.setClientId("admin-web");
            client.setClientName("Admin Web");
            client.setClientSecretHash(Sha256Helper.hashBase64Url("secret"));
            client.setStatus(OAuthClientStatus.ENABLED);
            client.setGrantTypes(new LinkedHashSet<>(Arrays.asList("authorization_code", "refresh_token")));
            client.setRedirectUris(new LinkedHashSet<>(Collections.singletonList("http://127.0.0.1/callback")));
            client.setScopes(new LinkedHashSet<>(Arrays.asList("openid", "profile")));
            client.setAccessTokenTtlSeconds(300L);
            client.setRefreshTokenTtlSeconds(600L);
            return client;
        }
    }

    private static class TestPrincipalAccessTokenDao implements PrincipalAccessTokenDao {

        private PrincipalAccessToken current;
        private PrincipalAccessToken inserted;
        private String currentToken;

        @Override
        public PrincipalAccessToken getById(PrincipalAccessTokenId id) {
            return current;
        }

        @Override
        public PrincipalAccessToken getByTokenCode(PrincipalAccessTokenCode tokenCode) {
            return current != null && current.getTokenCode().equals(tokenCode) ? current : null;
        }

        @Override
        public PrincipalAccessToken getByToken(String token) {
            return current != null && currentToken != null && currentToken.equals(token) ? current : null;
        }

        @Override
        public List<PrincipalAccessToken> listByPrincipalKeyAndClientIdAndStatus(
                PrincipalKey principalKey, String clientId, PrincipalTokenStatus status) {
            return current == null ? Collections.emptyList() : Collections.singletonList(current);
        }

        @Override
        public int countByClientIdAndStatus(String clientId, PrincipalTokenStatus status) {
            return current == null ? 0 : 1;
        }

        @Override
        public PrincipalAccessTokenId insert(PrincipalAccessToken accessToken, String token) {
            accessToken.setId(PrincipalAccessTokenIdCodec.toDomain("4001"));
            this.current = accessToken;
            this.inserted = accessToken;
            this.currentToken = token;
            return accessToken.getId();
        }

        @Override
        public int updateStatus(PrincipalAccessToken accessToken) {
            this.current = accessToken;
            return 1;
        }
    }

    private static class TestOAuthAuthorizationDao implements OAuthAuthorizationDao {

        private OAuthAuthorization current;

        @Override
        public OAuthAuthorization getById(EntityId id) {
            return current;
        }

        @Override
        public OAuthAuthorization getByAuthorizationCode(String authorizationCode) {
            return current != null && current.getAuthorizationCode().equals(authorizationCode) ? current : null;
        }

        @Override
        public EntityId insert(OAuthAuthorization authorization) {
            authorization.setId(EntityIdCodec.toDomain(5001L));
            this.current = authorization;
            return authorization.getId();
        }

        @Override
        public int updateUsed(OAuthAuthorization authorization) {
            this.current = authorization;
            return 1;
        }

        @Override
        public int deleteByAuthorizationCode(String authorizationCode) {
            if (current != null && current.getAuthorizationCode().equals(authorizationCode)) {
                current = null;
                return 1;
            }
            return 0;
        }
    }

    private static class TestPrincipalRefreshTokenDao implements PrincipalRefreshTokenDao {

        private PrincipalRefreshToken current;
        private PrincipalRefreshToken inserted;
        private String currentToken;

        @Override
        public PrincipalRefreshToken getById(PrincipalRefreshTokenId id) {
            return current;
        }

        @Override
        public PrincipalRefreshToken getByTokenCode(PrincipalRefreshTokenCode tokenCode) {
            return current;
        }

        @Override
        public PrincipalRefreshToken getByToken(String token) {
            return current != null && currentToken != null && currentToken.equals(token) ? current : null;
        }

        @Override
        public List<PrincipalRefreshToken> listByPrincipalKeyAndClientIdAndStatus(
                PrincipalKey principalKey, String clientId, PrincipalTokenStatus status) {
            return current == null ? Collections.emptyList() : Collections.singletonList(current);
        }

        @Override
        public PrincipalRefreshTokenId insert(PrincipalRefreshToken refreshToken, String token) {
            refreshToken.setId(PrincipalRefreshTokenIdCodec.toDomain("2002"));
            this.inserted = refreshToken;
            this.currentToken = token;
            return refreshToken.getId();
        }

        @Override
        public int updateStatus(PrincipalRefreshToken refreshToken) {
            this.current = refreshToken;
            return 1;
        }
    }

    private static class TestPrincipalAuthSessionDao implements PrincipalAuthSessionDao {

        private PrincipalAuthSession session;
        private int touchCount;

        @Override
        public PrincipalAuthSession getById(PrincipalAuthSessionId id) {
            return session != null && session.getId().equals(id) ? session : null;
        }

        @Override
        public void insert(PrincipalAuthSession session, int expireSeconds) {
            this.session = session;
        }

        @Override
        public void touch(PrincipalAuthSessionId id, Date accessTime, int expireSeconds) {
            if (session != null && session.getId().equals(id)) {
                session = PrincipalAuthSession.restore(
                        session.getId(),
                        session.getPrincipalKey(),
                        session.getClientId(),
                        session.getValues(),
                        session.getIssuedAt(),
                        accessTime,
                        session.getExpireAt());
                touchCount++;
            }
        }

        @Override
        public void deleteById(PrincipalAuthSessionId id) {
            if (session != null && session.getId().equals(id)) {
                session = null;
            }
        }

        private int getTouchCount() {
            return touchCount;
        }
    }

    private PrincipalAuthSession principalAuthSession(PrincipalAuthSessionId id, String clientId) {
        Date now = new Date();
        return PrincipalAuthSession.restore(
                id,
                PrincipalKey.of(PrincipalType.USER, 1L),
                clientId,
                null,
                now,
                now,
                new Date(now.getTime() + 60000L));
    }

    private static class TestUserService implements UserService {

        @Override
        public int changeStatus(ChangeUserStatusCommand command) {
            return 1;
        }

        @Override
        public List<com.github.thundax.modules.sys.entity.Role> listUserRoles(UserQuery query) {
            return Collections.emptyList();
        }

        @Override
        public User get(UserId id) {
            return user();
        }

        public List<User> listByIds(List<UserId> ids) {
            return Collections.singletonList(user());
        }

        @Override
        public List<User> list(UserQuery query) {
            return Collections.singletonList(user());
        }

        @Override
        public PageResult<User> page(UserQuery query, PageQuery page) {
            return PageResult.of(page.getPageNo(), page.getPageSize(), 0, Collections.emptyList());
        }

        @Override
        public UserId create(CreateUserCommand command) {
            return UserIdCodec.toDomain(1L);
        }

        @Override
        public void changeInfo(ChangeUserInfoCommand command) {}

        public int remove(DeleteUserCommand command) {
            return 1;
        }

        private User user() {
            User user = new User();
            user.setId(UserIdCodec.toDomain(1L));
            user.setStatus(UserStatus.ENABLED);
            user.setPrivilege(UserPrivilege.SUPER);
            user.setRank(AccessRank.of(0));
            return user;
        }
    }

    private static class TestPrincipalIdentityService implements PrincipalIdentityService {

        @Override
        public PrincipalIdentity get(PrincipalIdentityQuery query) {
            if (query.getIdentityValue() != null) {
                return identity(query.getIdentityValue());
            }
            return identity("tester");
        }

        @Override
        public List<PrincipalIdentity> list(PrincipalIdentityQuery query) {
            return Collections.singletonList(identity("tester"));
        }

        @Override
        public EntityId create(PrincipalIdentityCommand command) {
            PrincipalIdentity principalIdentity = command.getPrincipalIdentity();
            principalIdentity.setId(EntityId.of(8001L));
            return principalIdentity.getId();
        }

        @Override
        public void change(PrincipalIdentityCommand command) {}

        @Override
        public void changeStatus(PrincipalIdentityCommand command) {}

        private PrincipalIdentity identity(String loginName) {
            PrincipalIdentity identity = new PrincipalIdentity();
            identity.setId(EntityId.of(8001L));
            identity.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, 1L));
            identity.setType(PrincipalIdentityType.USER_ACCOUNT);
            identity.setIdentityValue(loginName);
            identity.setStatus(PrincipalIdentityStatus.ENABLED);
            return identity;
        }
    }

    private static class TestPrincipalCredentialService implements PrincipalCredentialService {

        @Override
        public PrincipalCredential get(PrincipalCredentialQuery query) {
            return credential();
        }

        @Override
        public List<PrincipalCredential> list(PrincipalCredentialQuery query) {
            return Collections.singletonList(credential());
        }

        @Override
        public EntityId create(PrincipalCredentialCommand command) {
            PrincipalCredential principalCredential = command.getPrincipalCredential();
            principalCredential.setId(EntityId.of(9001L));
            return principalCredential.getId();
        }

        @Override
        public void change(PrincipalCredentialCommand command) {}

        @Override
        public void changeStatus(PrincipalCredentialCommand command) {}

        @Override
        public void changeVerifyState(PrincipalCredentialCommand command) {}

        private PrincipalCredential credential() {
            PrincipalCredential credential = new PrincipalCredential();
            credential.setId(EntityId.of(9001L));
            credential.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, 1L));
            credential.setIdentityId(EntityId.of(8001L));
            credential.setCredentialType(PrincipalCredentialType.USER_PASSWORD);
            credential.setCredentialValue("secret");
            credential.setStatus(PrincipalCredentialStatus.ACTIVE);
            credential.setFailedLimit(3);
            return credential;
        }
    }

    private static class TestMenuService implements MenuService {

        @Override
        public int changeVisibility(ChangeMenuVisibilityCommand command) {
            return 1;
        }

        @Override
        public void move(MoveMenuCommand command) {}

        @Override
        public boolean existsChildRelation(MenuQuery query) {
            return false;
        }

        @Override
        public Menu get(MenuId id) {
            return menus().get(0);
        }

        @Override
        public List<Menu> list(MenuQuery query) {
            return menus();
        }

        @Override
        public PageResult<Menu> page(MenuQuery query, PageQuery page) {
            return PageResult.of(page.getPageNo(), page.getPageSize(), 0, Collections.emptyList());
        }

        @Override
        public MenuId create(CreateMenuCommand command) {
            return MenuIdCodec.toDomain(6001L);
        }

        @Override
        public void changeInfo(ChangeMenuInfoCommand command) {}

        public int remove(DeleteMenuCommand command) {
            return 1;
        }

        private List<Menu> menus() {
            Menu menu = new Menu();
            menu.setId(MenuIdCodec.toDomain(6001L));
            menu.setPerms("sys:role,sys:user:view");
            menu.setName("system");
            menu.setRank(AccessRank.of(0));
            return Arrays.asList(menu);
        }
    }

    private static class TestRoleService implements RoleService {

        @Override
        public int changeStatus(ChangeRoleStatusCommand command) {
            return 1;
        }

        @Override
        public void assignUsers(AssignRoleUsersCommand command) {}

        @Override
        public List<User> listRoleUsers(RoleQuery query) {
            return Collections.emptyList();
        }

        @Override
        public List<Menu> listRoleMenus(RoleQuery query) {
            return Collections.emptyList();
        }

        private com.github.thundax.modules.sys.entity.Role role(RoleId id) {
            com.github.thundax.modules.sys.entity.Role role = new com.github.thundax.modules.sys.entity.Role();
            role.setId(id);
            return role;
        }

        @Override
        public com.github.thundax.modules.sys.entity.Role get(RoleId id) {
            return role(id);
        }

        public List<com.github.thundax.modules.sys.entity.Role> listByIds(List<RoleId> ids) {
            return Collections.emptyList();
        }

        public List<com.github.thundax.modules.sys.entity.Role> list(
                com.github.thundax.modules.sys.entity.Role entity) {
            return Collections.emptyList();
        }

        @Override
        public List<com.github.thundax.modules.sys.entity.Role> list(RoleQuery query) {
            return Collections.emptyList();
        }

        @Override
        public PageResult<com.github.thundax.modules.sys.entity.Role> page(RoleQuery query, PageQuery page) {
            return PageResult.of(page.getPageNo(), page.getPageSize(), 0, Collections.emptyList());
        }

        @Override
        public RoleId create(CreateRoleCommand command) {
            return RoleIdCodec.toDomain(10001L);
        }

        @Override
        public void changeInfo(ChangeRoleInfoCommand command) {}

        public int remove(DeleteRoleCommand command) {
            return 1;
        }

        public int changePriority(ChangeRolePriorityCommand command) {
            return 1;
        }
    }
}
