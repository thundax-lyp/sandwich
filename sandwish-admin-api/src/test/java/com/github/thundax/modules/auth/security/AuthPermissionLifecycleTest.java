package com.github.thundax.modules.auth.security;

import com.github.thundax.autoconfigure.LoginProperties;
import com.github.thundax.autoconfigure.SandwishProperties;
import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.common.utils.encrypt.Sha256Helper;
import com.github.thundax.modules.auth.assembler.AuthInterfaceAssembler;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.controller.response.OAuth2IntrospectionResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2UserinfoResponse;
import com.github.thundax.modules.auth.dao.AuthSessionDao;
import com.github.thundax.modules.auth.dao.AuthSessionRuntimeDao;
import com.github.thundax.modules.auth.dao.OAuthAccessTokenDao;
import com.github.thundax.modules.auth.dao.OAuthAuthorizationDao;
import com.github.thundax.modules.auth.dao.OAuthClientDao;
import com.github.thundax.modules.auth.dao.OAuthRefreshTokenDao;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.entity.AuthSession;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.entity.OAuthAccessToken;
import com.github.thundax.modules.auth.entity.OAuthAuthorization;
import com.github.thundax.modules.auth.entity.OAuthClient;
import com.github.thundax.modules.auth.entity.OAuthRefreshToken;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.AuthSessionStatus;
import com.github.thundax.modules.auth.entity.enums.OAuthAccessTokenStatus;
import com.github.thundax.modules.auth.entity.enums.OAuthClientStatus;
import com.github.thundax.modules.auth.entity.enums.OAuthRefreshTokenStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.security.filter.AccessTokenAuthenticationFilter;
import com.github.thundax.modules.auth.service.AdminAuthService;
import com.github.thundax.modules.auth.service.PermissionService;
import com.github.thundax.modules.auth.service.PrincipalAuthService;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.github.thundax.modules.auth.service.impl.AdminAuthServiceImpl;
import com.github.thundax.modules.auth.service.impl.PermissionServiceImpl;
import com.github.thundax.modules.auth.service.provider.GithubLoginProvider;
import com.github.thundax.modules.auth.service.provider.WecomLoginProvider;
import com.github.thundax.modules.auth.service.result.AuthTokenQueryResult;
import com.github.thundax.modules.auth.service.result.AuthTokenRefreshResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationDecisionResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationViewResult;
import com.github.thundax.modules.auth.testsupport.InMemoryAccessTokenDaoImpl;
import com.github.thundax.modules.auth.testsupport.InMemoryLoginFormDaoImpl;
import com.github.thundax.modules.auth.testsupport.InMemoryPermissionDaoImpl;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.impl.CurrentUserServiceImpl;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthPermissionLifecycleTest {

    private InMemoryAccessTokenDaoImpl accessTokenDao;
    private InMemoryPermissionDaoImpl permissionDao;
    private TestAuthSessionDao authSessionDao;
    private TestAuthSessionRuntimeDao authSessionRuntimeDao;
    private InMemoryLoginFormDaoImpl loginFormDao;
    private AdminAuthService authService;
    private PermissionService permissionService;

    @Before
    public void setUp() {
        accessTokenDao = new InMemoryAccessTokenDaoImpl();
        permissionDao = new InMemoryPermissionDaoImpl();
        loginFormDao = new InMemoryLoginFormDaoImpl();
        authSessionDao = new TestAuthSessionDao();
        authSessionRuntimeDao = new TestAuthSessionRuntimeDao();

        AuthProperties authProperties = new AuthProperties();
        authProperties.setLoginExpiredSeconds(60);

        TestUserService userService = new TestUserService();
        TestPrincipalIdentityService principalIdentityService = new TestPrincipalIdentityService();
        TestPrincipalCredentialService principalCredentialService = new TestPrincipalCredentialService();
        permissionService = new PermissionServiceImpl(
                permissionDao,
                authProperties,
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
                loginFormDao,
                accessTokenDao,
                authSessionDao,
                authSessionRuntimeDao,
                permissionService,
                new TestPrincipalAuthService(),
                principalIdentityService,
                userService);
    }

    @After
    public void tearDown() {
        UserAccessHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldCreateTouchAndReleasePermissionSessionWithAccessToken() {
        AccessToken accessToken = authService.createAccessToken("1", "tester");

        Assert.assertNotNull(permissionService.getSession(accessToken.getToken()));
        Assert.assertNotNull(authSessionDao.getByToken(accessToken.getToken()));
        Assert.assertNotNull(authSessionRuntimeDao.getByToken(accessToken.getToken()));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "sys:role:view"));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "user"));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "admin"));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "super"));
        Date databaseLastAccessTime =
                authSessionDao.getByToken(accessToken.getToken()).getLastAccessTime();

        authService.activeAccessToken(accessToken);
        Assert.assertTrue(permissionDao.getTouchCount() > 0);
        Assert.assertEquals(0, authSessionDao.getTouchCount());
        Assert.assertTrue(authSessionRuntimeDao.getTouchCount() > 0);
        Assert.assertEquals(
                databaseLastAccessTime,
                authSessionDao.getByToken(accessToken.getToken()).getLastAccessTime());

        Date runtimeLastAccessTime =
                authSessionRuntimeDao.getByToken(accessToken.getToken()).getLastAccessTime();
        authService.deleteAccessToken(accessToken);
        Assert.assertNull(permissionService.getSession(accessToken.getToken()));
        Assert.assertNull(authSessionRuntimeDao.getByToken(accessToken.getToken()));
        Assert.assertEquals(
                AuthSessionStatus.LOGGED_OUT,
                authSessionDao.getByToken(accessToken.getToken()).getStatus());
        Assert.assertEquals(
                runtimeLastAccessTime,
                authSessionDao.getByToken(accessToken.getToken()).getLastAccessTime());
    }

    @Test
    public void shouldInvalidateSessionByUserId() {
        AccessToken accessToken = authService.createAccessToken("1", "tester");

        authService.invalidateSessionsByUserId(EntityIdCodec.toDomain(1L), "PASSWORD_RESET");

        Assert.assertNull(accessTokenDao.getByUserId("1"));
        Assert.assertNull(permissionService.getSession(accessToken.getToken()));
        Assert.assertNull(authSessionRuntimeDao.getByToken(accessToken.getToken()));
        Assert.assertEquals(
                AuthSessionStatus.INVALIDATED,
                authSessionDao.getByToken(accessToken.getToken()).getStatus());
        Assert.assertEquals(
                "PASSWORD_RESET",
                authSessionDao.getByToken(accessToken.getToken()).getInvalidateReason());
    }

    @Test
    public void shouldQueryTokenActiveStateAndUserinfo() {
        AccessToken accessToken = authService.createAccessToken("1", "tester");

        AuthTokenQueryResult result = authService.queryToken(accessToken.getToken());

        Assert.assertTrue(result.isActive());
        Assert.assertEquals(accessToken.getToken(), result.getSession().getToken());
        Assert.assertEquals("tester", result.getUsername());
        Assert.assertFalse(authService.queryToken("missing").isActive());
    }

    @Test
    public void shouldRefreshAccessTokenAndRotateRefreshToken() throws Exception {
        TestOAuthRefreshTokenDao refreshTokenDao = new TestOAuthRefreshTokenDao();
        inject(authService, "oauthRefreshTokenDao", refreshTokenDao);
        inject(authService, "oauthClientDao", new TestOAuthClientDao());

        OAuthRefreshToken refreshToken = new OAuthRefreshToken();
        refreshToken.setId(EntityIdCodec.toDomain(2001L));
        refreshToken.setTokenId("refresh-token-1");
        refreshToken.setTokenHash(Sha256Helper.hashBase64Url("plain-refresh-token"));
        refreshToken.setAccessTokenId("old-access-token");
        refreshToken.setClientId("admin-web");
        refreshToken.setUserId(EntityIdCodec.toDomain(1L));
        refreshToken.setIssuedAt(new Date(1000L));
        refreshToken.setExpireAt(new Date(System.currentTimeMillis() + 60000L));
        refreshToken.setStatus(OAuthRefreshTokenStatus.ACTIVE);
        refreshTokenDao.current = refreshToken;

        AuthTokenRefreshResult result = authService.refreshAccessToken("admin-web", "plain-refresh-token");

        Assert.assertNotNull(result.getAccessToken().getToken());
        Assert.assertNotNull(result.getRefreshToken());
        Assert.assertEquals(OAuthRefreshTokenStatus.USED, refreshToken.getStatus());
        Assert.assertEquals(OAuthRefreshTokenStatus.ACTIVE, refreshTokenDao.inserted.getStatus());
    }

    @Test
    public void shouldAuthorizeApproveExchangeAndRevokeAuthorizationCode() throws Exception {
        TestOAuthAuthorizationDao authorizationDao = new TestOAuthAuthorizationDao();
        TestOAuthRefreshTokenDao refreshTokenDao = new TestOAuthRefreshTokenDao();
        TestOAuthAccessTokenDao accessTokenDao = new TestOAuthAccessTokenDao();
        inject(authService, "oauthAuthorizationDao", authorizationDao);
        inject(authService, "oauthAccessTokenDao", accessTokenDao);
        inject(authService, "oauthRefreshTokenDao", refreshTokenDao);
        inject(authService, "oauthClientDao", new TestOAuthClientDao());

        OAuth2AuthorizationViewResult view = authService.authorizeOAuth2(
                "admin-web", "http://127.0.0.1/callback", Arrays.asList("openid", "profile"), "state-1");

        Assert.assertEquals("admin-web", view.getClientId());
        Assert.assertEquals("Admin Web", view.getClientName());
        Assert.assertTrue(view.getScopes().contains("openid"));

        String codeVerifier = "plain-verifier";
        String codeChallenge = Sha256Helper.hashBase64Url(codeVerifier);
        OAuth2AuthorizationDecisionResult decision = authService.decideOAuth2(
                "admin-web",
                "http://127.0.0.1/callback",
                Arrays.asList("openid", "profile"),
                "state-1",
                codeChallenge,
                "S256",
                "1",
                true);

        Assert.assertTrue(decision.isApproved());
        Assert.assertNotNull(decision.getAuthorizationCode());
        Assert.assertEquals("state-1", decision.getState());
        Assert.assertFalse(authorizationDao.current.isUsed());

        AuthTokenRefreshResult token = authService.exchangeOAuth2Token(
                "admin-web",
                "secret",
                "authorization_code",
                "http://127.0.0.1/callback",
                decision.getAuthorizationCode(),
                codeVerifier,
                null);

        Assert.assertNotNull(token.getAccessToken().getToken());
        Assert.assertNotNull(token.getRefreshToken());
        Assert.assertNotNull(token.getOauthAccessToken());
        Assert.assertTrue(authorizationDao.current.isUsed());
        Assert.assertEquals(OAuthAccessTokenStatus.ACTIVE, accessTokenDao.inserted.getStatus());
        Assert.assertEquals(OAuthRefreshTokenStatus.ACTIVE, refreshTokenDao.inserted.getStatus());
        AuthTokenQueryResult queryResult = authService.queryToken(token.getOauthAccessToken());
        Assert.assertTrue(queryResult.isActive());
        OAuth2IntrospectionResponse introspection = AuthInterfaceAssembler.toIntrospectionResponse(queryResult);
        Assert.assertEquals("admin-web", introspection.getClientId());
        Assert.assertEquals("openid profile", introspection.getScope());
        Assert.assertEquals("Bearer", introspection.getTokenType());
        Assert.assertTrue(introspection.getExpiresAt() > 0L);
        OAuth2UserinfoResponse userinfo = AuthInterfaceAssembler.toUserinfoResponse(queryResult);
        Assert.assertEquals("tester", userinfo.getPreferredUsername());
        Assert.assertTrue(authService.revokeOAuth2Token("admin-web", "secret", token.getOauthAccessToken()));
        Assert.assertFalse(authService.queryToken(token.getOauthAccessToken()).isActive());
        Assert.assertTrue(authService.revokeAuthorizationCode(decision.getAuthorizationCode()));
    }

    @Test
    public void shouldExchangeRefreshTokenGrantAndRotateOAuthTokens() throws Exception {
        TestOAuthAccessTokenDao accessTokenDao = new TestOAuthAccessTokenDao();
        TestOAuthRefreshTokenDao refreshTokenDao = new TestOAuthRefreshTokenDao();
        inject(authService, "oauthAccessTokenDao", accessTokenDao);
        inject(authService, "oauthRefreshTokenDao", refreshTokenDao);
        inject(authService, "oauthClientDao", new TestOAuthClientDao());

        OAuthRefreshToken refreshToken = new OAuthRefreshToken();
        refreshToken.setId(EntityIdCodec.toDomain(2001L));
        refreshToken.setTokenId("refresh-token-1");
        refreshToken.setTokenHash(Sha256Helper.hashBase64Url("plain-refresh-token"));
        refreshToken.setAccessTokenId("old-access-token");
        refreshToken.setClientId("admin-web");
        refreshToken.setUserId(EntityIdCodec.toDomain(1L));
        refreshToken.setIssuedAt(new Date(1000L));
        refreshToken.setExpireAt(new Date(System.currentTimeMillis() + 60000L));
        refreshToken.setStatus(OAuthRefreshTokenStatus.ACTIVE);
        refreshTokenDao.current = refreshToken;

        AuthTokenRefreshResult result = authService.exchangeOAuth2Token(
                "admin-web", "secret", "refresh_token", null, null, null, "plain-refresh-token");

        Assert.assertEquals(OAuthRefreshTokenStatus.USED, refreshToken.getStatus());
        Assert.assertNotNull(result.getOauthAccessToken());
        Assert.assertEquals(OAuthAccessTokenStatus.ACTIVE, accessTokenDao.inserted.getStatus());
        Assert.assertEquals(OAuthRefreshTokenStatus.ACTIVE, refreshTokenDao.inserted.getStatus());
    }

    @Test
    public void shouldRejectAuthorizationWhenClientScopeIsInvalid() throws Exception {
        inject(authService, "oauthClientDao", new TestOAuthClientDao());

        try {
            authService.authorizeOAuth2(
                    "admin-web", "http://127.0.0.1/callback", Collections.singletonList("admin.write"), "state-1");
            Assert.fail("invalid scope must be rejected");
        } catch (ApiException expected) {
            Assert.assertNotNull(expected);
        }
    }

    @Test
    public void shouldAuthenticateSmsWecomAndGithubIdentity() throws Exception {
        LoginForm form = authService.createLoginForm();
        String smsCode = authService.createSmsValidateCode(form.getLoginToken(), "13800000000");

        Assert.assertEquals(
                Long.valueOf(1L),
                EntityIdCodec.toValue(authService
                        .authenticateSms(form.getLoginToken(), "13800000000", smsCode)
                        .getId()));

        inject(authService, "wecomLoginProvider", (WecomLoginProvider) code -> "wecom-user-1");
        inject(authService, "githubLoginProvider", (GithubLoginProvider) code -> "github-user-1");

        Assert.assertEquals(
                Long.valueOf(1L),
                EntityIdCodec.toValue(
                        authService.authenticateWecom("wecom-code").getId()));
        Assert.assertEquals(
                Long.valueOf(1L),
                EntityIdCodec.toValue(
                        authService.authenticateGithub("github-code").getId()));
    }

    @Test
    public void shouldAuthenticateRequestAndPopulateSpringSecurityContext() throws Exception {
        AccessToken accessToken = authService.createAccessToken("1", "tester");
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
        Assert.assertTrue(permissionDao.getTouchCount() > 0);
        Assert.assertTrue(accessTokenDao.getActiveCount() > 0);
        Assert.assertEquals(0, authSessionDao.getTouchCount());
        Assert.assertTrue(authSessionRuntimeDao.getTouchCount() > 0);
    }

    @Test
    public void shouldClearUserAccessHolderAfterAuthenticatedRequest() throws Exception {
        AccessToken accessToken = authService.createAccessToken("1", "tester");
        AccessTokenAuthenticationFilter filter = new AccessTokenAuthenticationFilter(
                new SandwishProperties.AccessTokenFilterProperties(),
                authService,
                permissionService,
                new TestUserService());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sys/user");
        request.addHeader(Constants.HEADER_TOKEN, accessToken.getToken());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            Assert.assertEquals("1", UserAccessHolder.currentUserId());
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
        public PrincipalIdentity authenticateIdentity(PrincipalIdentityType identityType, String identityValue) {
            return identity(identityType, identityValue);
        }

        @Override
        public PrincipalIdentity authenticatePassword(
                PrincipalIdentityType identityType,
                String identityValue,
                PrincipalCredentialType credentialType,
                String plainPassword,
                PrincipalPasswordPolicyDTO passwordPolicy) {
            return identity(identityType, identityValue);
        }

        private PrincipalIdentity identity(PrincipalIdentityType identityType, String identityValue) {
            PrincipalIdentity identity = new PrincipalIdentity();
            identity.setId(EntityIdCodec.toDomain(1001L));
            identity.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, EntityIdCodec.toDomain(1L)));
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

    private static class TestOAuthAccessTokenDao implements OAuthAccessTokenDao {

        private OAuthAccessToken current;
        private OAuthAccessToken inserted;

        @Override
        public OAuthAccessToken getById(EntityId id) {
            return current;
        }

        @Override
        public OAuthAccessToken getByTokenId(String tokenId) {
            return current != null && current.getTokenId().equals(tokenId) ? current : null;
        }

        @Override
        public OAuthAccessToken getByTokenHash(String tokenHash) {
            return current != null && current.getTokenHash().equals(tokenHash) ? current : null;
        }

        @Override
        public EntityId insert(OAuthAccessToken accessToken) {
            accessToken.setId(EntityIdCodec.toDomain(4001L));
            this.current = accessToken;
            this.inserted = accessToken;
            return accessToken.getId();
        }

        @Override
        public int updateStatus(OAuthAccessToken accessToken) {
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

    private static class TestOAuthRefreshTokenDao implements OAuthRefreshTokenDao {

        private OAuthRefreshToken current;
        private OAuthRefreshToken inserted;

        @Override
        public OAuthRefreshToken getById(EntityId id) {
            return current;
        }

        @Override
        public OAuthRefreshToken getByTokenId(String tokenId) {
            return current;
        }

        @Override
        public OAuthRefreshToken getByTokenHash(String tokenHash) {
            return current != null && current.getTokenHash().equals(tokenHash) ? current : null;
        }

        @Override
        public List<OAuthRefreshToken> listByClientIdAndUserIdAndStatus(
                String clientId, EntityId userId, OAuthRefreshTokenStatus status) {
            return current == null ? Collections.emptyList() : Collections.singletonList(current);
        }

        @Override
        public EntityId insert(OAuthRefreshToken refreshToken) {
            refreshToken.setId(EntityIdCodec.toDomain(2002L));
            this.inserted = refreshToken;
            return refreshToken.getId();
        }

        @Override
        public int updateStatus(OAuthRefreshToken refreshToken) {
            this.current = refreshToken;
            return 1;
        }
    }

    private static class TestAuthSessionDao implements AuthSessionDao {

        private AuthSession session;
        private int touchCount;

        public AuthSession getById(EntityId id) {
            return session;
        }

        @Override
        public AuthSession getBySessionId(String sessionId) {
            return session;
        }

        @Override
        public AuthSession getByToken(String token) {
            return session != null && session.getToken().equals(token) ? session : null;
        }

        @Override
        public List<AuthSession> listByUserIdAndStatus(EntityId userId, AuthSessionStatus status) {
            if (session == null || (status != null && session.getStatus() != status)) {
                return Collections.emptyList();
            }
            return Collections.singletonList(session);
        }

        @Override
        public EntityId insert(AuthSession authSession) {
            authSession.setId(EntityId.of(7001L));
            this.session = authSession;
            return authSession.getId();
        }

        @Override
        public int updateAccessTime(AuthSession authSession) {
            this.session = authSession;
            touchCount++;
            return 1;
        }

        @Override
        public int updateLogout(AuthSession authSession) {
            this.session = authSession;
            return 1;
        }

        @Override
        public int updateInvalidate(AuthSession authSession) {
            this.session = authSession;
            return 1;
        }

        @Override
        public int updateExpire(AuthSession authSession) {
            this.session = authSession;
            return 1;
        }

        private int getTouchCount() {
            return touchCount;
        }
    }

    private static class TestAuthSessionRuntimeDao implements AuthSessionRuntimeDao {

        private AuthSession session;
        private int touchCount;

        @Override
        public AuthSession getByToken(String token) {
            return session != null && session.getToken().equals(token) ? session : null;
        }

        @Override
        public void insert(AuthSession authSession, int expiredSeconds) {
            this.session = copy(authSession);
        }

        @Override
        public void touch(String token, Date accessTime, int expiredSeconds) {
            if (session != null && session.getToken().equals(token)) {
                session.touch(accessTime);
                touchCount++;
            }
        }

        @Override
        public void deleteByToken(String token) {
            if (session != null && session.getToken().equals(token)) {
                session = null;
            }
        }

        private int getTouchCount() {
            return touchCount;
        }

        private AuthSession copy(AuthSession source) {
            AuthSession target = new AuthSession();
            target.setId(source.getId());
            target.setSessionId(source.getSessionId());
            target.setToken(source.getToken());
            target.setUserId(source.getUserId());
            target.setIdentityId(source.getIdentityId());
            target.setIdentityType(source.getIdentityType());
            target.setLoginType(source.getLoginType());
            target.setStatus(source.getStatus());
            target.setIssuedAt(source.getIssuedAt());
            target.setLastAccessTime(source.getLastAccessTime());
            target.setExpireAt(source.getExpireAt());
            target.setLogoutAt(source.getLogoutAt());
            target.setInvalidateReason(source.getInvalidateReason());
            return target;
        }
    }

    private static class TestUserService implements UserService {

        @Override
        public int updateStatus(User user) {
            return 1;
        }

        @Override
        public int batchUpdateStatus(List<User> list) {
            return list.size();
        }

        @Override
        public List<com.github.thundax.modules.sys.entity.Role> listUserRoles(User user) {
            return Collections.emptyList();
        }

        public User getById(EntityId id) {
            return user();
        }

        public List<User> listByIds(List<EntityId> ids) {
            return Collections.singletonList(user());
        }

        public List<User> listAll() {
            return Collections.singletonList(user());
        }

        public List<User> list(UserQuery query) {
            return Collections.singletonList(user());
        }

        public PageDTO<User> page(UserQuery query, PageDTO<User> page) {
            return page;
        }

        @Override
        public EntityId add(User entity, String loginName, String encryptedPassword, List<Long> roleIdList) {
            return EntityId.of(1L);
        }

        @Override
        public void update(User entity, String loginName, List<Long> roleIdList) {}

        public int deleteById(EntityId id) {
            return 1;
        }

        public int batchDeleteById(List<EntityId> ids) {
            return ids.size();
        }

        private User user() {
            User user = new User();
            user.setId(EntityIdCodec.toDomain(1L));
            user.setStatus(UserStatus.ENABLED);
            user.setPrivilege(UserPrivilege.SUPER);
            user.setRank(AccessRank.of(0));
            return user;
        }
    }

    private static class TestPrincipalIdentityService implements PrincipalIdentityService {

        @Override
        public PrincipalIdentity getById(EntityId id) {
            return identity("tester");
        }

        @Override
        public PrincipalIdentity getByIdentity(PrincipalIdentityType identityType, String identityValue) {
            return identity(identityValue);
        }

        @Override
        public PrincipalIdentity getByPrincipalKeyAndType(
                PrincipalKey principalKey, PrincipalIdentityType identityType) {
            return identity("tester");
        }

        @Override
        public List<PrincipalIdentity> listByPrincipalKeyAndStatus(
                PrincipalKey principalKey, PrincipalIdentityStatus status) {
            return Collections.singletonList(identity("tester"));
        }

        @Override
        public EntityId add(PrincipalIdentity principalIdentity) {
            principalIdentity.setId(EntityId.of(8001L));
            return principalIdentity.getId();
        }

        @Override
        public void update(PrincipalIdentity principalIdentity) {}

        @Override
        public void updateStatus(PrincipalIdentity principalIdentity) {}

        private PrincipalIdentity identity(String loginName) {
            PrincipalIdentity identity = new PrincipalIdentity();
            identity.setId(EntityId.of(8001L));
            identity.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, EntityId.of(1L)));
            identity.setType(PrincipalIdentityType.USER_ACCOUNT);
            identity.setIdentityValue(loginName);
            identity.setStatus(PrincipalIdentityStatus.ENABLED);
            return identity;
        }
    }

    private static class TestPrincipalCredentialService implements PrincipalCredentialService {

        @Override
        public PrincipalCredential getById(EntityId id) {
            return credential();
        }

        @Override
        public PrincipalCredential getByIdentityIdAndType(EntityId identityId, PrincipalCredentialType credentialType) {
            return credential();
        }

        @Override
        public PrincipalCredential getByPrincipalKeyAndType(
                PrincipalKey principalKey, PrincipalCredentialType credentialType) {
            return credential();
        }

        @Override
        public List<PrincipalCredential> listByPrincipalKeyAndStatus(
                PrincipalKey principalKey, PrincipalCredentialStatus status) {
            return Collections.singletonList(credential());
        }

        @Override
        public EntityId add(PrincipalCredential principalCredential) {
            principalCredential.setId(EntityId.of(9001L));
            return principalCredential.getId();
        }

        @Override
        public void update(PrincipalCredential principalCredential) {}

        @Override
        public void updateStatus(PrincipalCredential principalCredential) {}

        @Override
        public void updateVerifyState(PrincipalCredential principalCredential) {}

        private PrincipalCredential credential() {
            PrincipalCredential credential = new PrincipalCredential();
            credential.setId(EntityId.of(9001L));
            credential.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, EntityId.of(1L)));
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
        public int updateVisibility(Menu menu) {
            return 1;
        }

        @Override
        public int batchUpdateVisibility(List<Menu> list) {
            return list.size();
        }

        @Override
        public void moveTreeNode(Menu fromBean, Menu toBean, TreeNodeMoveType moveType) {}

        @Override
        public boolean isChildOf(Menu child, Menu parent) {
            return false;
        }

        public Menu getById(EntityId id) {
            return menus().get(0);
        }

        public List<Menu> listByIds(List<EntityId> ids) {
            return menus();
        }

        public List<Menu> list(MenuQuery query) {
            return menus();
        }

        public PageDTO<Menu> page(MenuQuery query, PageDTO<Menu> page) {
            return page;
        }

        @Override
        public EntityId add(Menu entity) {
            return EntityId.of(6001L);
        }

        @Override
        public void update(Menu entity) {}

        public int deleteById(EntityId id) {
            return 1;
        }

        public int batchDeleteById(List<EntityId> ids) {
            return ids.size();
        }

        private List<Menu> menus() {
            Menu menu = new Menu();
            menu.setId(EntityIdCodec.toDomain(6001L));
            menu.setPerms("sys:role,sys:user:view");
            menu.setName("system");
            menu.setRank(AccessRank.of(0));
            return Arrays.asList(menu);
        }
    }

    private static class TestRoleService implements RoleService {

        @Override
        public List<com.github.thundax.modules.sys.entity.Role> listEnabled() {
            return Collections.emptyList();
        }

        @Override
        public int updateStatus(com.github.thundax.modules.sys.entity.Role role) {
            return 1;
        }

        @Override
        public int batchUpdateStatus(List<com.github.thundax.modules.sys.entity.Role> list) {
            return list.size();
        }

        @Override
        public void updateUserList(com.github.thundax.modules.sys.entity.Role role, List<User> userList) {}

        @Override
        public List<User> listRoleUsers(com.github.thundax.modules.sys.entity.Role role) {
            return Collections.emptyList();
        }

        @Override
        public List<Menu> listRoleMenus(com.github.thundax.modules.sys.entity.Role role) {
            return Collections.emptyList();
        }

        private com.github.thundax.modules.sys.entity.Role role(Long id) {
            com.github.thundax.modules.sys.entity.Role role = new com.github.thundax.modules.sys.entity.Role();
            role.setId(EntityIdCodec.toDomain(id));
            return role;
        }

        public com.github.thundax.modules.sys.entity.Role getById(EntityId id) {
            return role(id.value());
        }

        public List<com.github.thundax.modules.sys.entity.Role> listByIds(List<EntityId> ids) {
            return Collections.emptyList();
        }

        public List<com.github.thundax.modules.sys.entity.Role> list(
                com.github.thundax.modules.sys.entity.Role entity) {
            return Collections.emptyList();
        }

        public List<com.github.thundax.modules.sys.entity.Role> list(RoleQuery query) {
            return Collections.emptyList();
        }

        public PageDTO<com.github.thundax.modules.sys.entity.Role> page(
                RoleQuery query, PageDTO<com.github.thundax.modules.sys.entity.Role> page) {
            return page;
        }

        @Override
        public EntityId add(com.github.thundax.modules.sys.entity.Role entity) {
            return EntityId.of(10001L);
        }

        @Override
        public void update(com.github.thundax.modules.sys.entity.Role entity) {}

        public int deleteById(EntityId id) {
            return 1;
        }

        public int batchDeleteById(List<EntityId> ids) {
            return ids.size();
        }

        public int updatePriority(List<com.github.thundax.modules.sys.entity.Role> list) {
            return list.size();
        }
    }
}
