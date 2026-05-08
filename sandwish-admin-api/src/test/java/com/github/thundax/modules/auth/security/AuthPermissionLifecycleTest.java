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
import com.github.thundax.modules.auth.codec.PrincipalAccessTokenIdCodec;
import com.github.thundax.modules.auth.codec.PrincipalRefreshTokenIdCodec;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.controller.response.OAuth2IntrospectionResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2UserinfoResponse;
import com.github.thundax.modules.auth.dao.OAuthAuthorizationDao;
import com.github.thundax.modules.auth.dao.OAuthClientDao;
import com.github.thundax.modules.auth.dao.PrincipalAccessTokenDao;
import com.github.thundax.modules.auth.dao.PrincipalAuthSessionDao;
import com.github.thundax.modules.auth.dao.PrincipalRefreshTokenDao;
import com.github.thundax.modules.auth.entity.OAuthAuthorization;
import com.github.thundax.modules.auth.entity.OAuthClient;
import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.entity.PrincipalAuthSession;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.PrincipalRefreshToken;
import com.github.thundax.modules.auth.entity.enums.OAuthClientStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalTokenStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalAccessTokenCode;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalAccessTokenId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalRefreshTokenCode;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalRefreshTokenId;
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
import com.github.thundax.modules.auth.service.result.AuthAccessTokenResult;
import com.github.thundax.modules.auth.service.result.AuthTokenQueryResult;
import com.github.thundax.modules.auth.service.result.AuthTokenRefreshResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationDecisionResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationViewResult;
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

    private TestPrincipalAccessTokenDao accessTokenDao;
    private TestPrincipalRefreshTokenDao refreshTokenDao;
    private InMemoryPermissionDaoImpl permissionDao;
    private TestPrincipalAuthSessionDao principalAuthSessionDao;
    private AdminAuthService authService;
    private PermissionService permissionService;

    @Before
    public void setUp() throws Exception {
        accessTokenDao = new TestPrincipalAccessTokenDao();
        refreshTokenDao = new TestPrincipalRefreshTokenDao();
        permissionDao = new InMemoryPermissionDaoImpl();
        principalAuthSessionDao = new TestPrincipalAuthSessionDao();

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
    public void shouldCreateTouchAndReleasePermissionSessionWithAccessToken() {
        AuthAccessTokenResult accessToken = authService.createAccessToken("1", "tester");

        Assert.assertNotNull(permissionService.getSession(accessToken.getToken()));
        Assert.assertNotNull(principalAuthSessionDao.getById(
                accessToken.getPrincipalAccessToken().getSessionId()));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "sys:role:view"));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "user"));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "admin"));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "super"));
        Date databaseLastAccessTime = principalAuthSessionDao
                .getById(accessToken.getPrincipalAccessToken().getSessionId())
                .getLastAccessTime();

        authService.activeAccessToken(accessToken);
        Assert.assertTrue(permissionDao.getTouchCount() > 0);
        Assert.assertTrue(principalAuthSessionDao.getTouchCount() > 0);
        Assert.assertTrue(principalAuthSessionDao
                        .getById(accessToken.getPrincipalAccessToken().getSessionId())
                        .getLastAccessTime()
                        .getTime()
                >= databaseLastAccessTime.getTime());

        authService.deleteAccessToken(accessToken);
        Assert.assertNull(permissionService.getSession(accessToken.getToken()));
        Assert.assertNull(principalAuthSessionDao.getById(
                accessToken.getPrincipalAccessToken().getSessionId()));
    }

    @Test
    public void shouldInvalidateSessionByUserId() {
        AuthAccessTokenResult accessToken = authService.createAccessToken("1", "tester");

        authService.invalidateSessionsByUserId(EntityIdCodec.toDomain(1L), "PASSWORD_RESET");

        Assert.assertEquals(
                PrincipalTokenStatus.REVOKED,
                accessToken.getPrincipalAccessToken().getStatus());
        Assert.assertNull(principalAuthSessionDao.getById(
                accessToken.getPrincipalAccessToken().getSessionId()));
    }

    @Test
    public void shouldQueryTokenActiveStateAndUserinfo() {
        AuthAccessTokenResult accessToken = authService.createAccessToken("1", "tester");

        AuthTokenQueryResult result = authService.queryToken(accessToken.getToken());

        Assert.assertTrue(result.isActive());
        Assert.assertEquals(
                accessToken.getPrincipalAccessToken().getSessionId(),
                result.getSession().getId());
        Assert.assertEquals("tester", result.getUsername());
        Assert.assertFalse(authService.queryToken("missing").isActive());
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
        refreshToken.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, EntityIdCodec.toDomain(1L)));
        refreshToken.setIssuedAt(new Date(1000L));
        refreshToken.setExpireAt(new Date(System.currentTimeMillis() + 60000L));
        refreshToken.setStatus(PrincipalTokenStatus.ACTIVE);
        refreshTokenDao.current = refreshToken;
        refreshTokenDao.currentToken = "plain-refresh-token";

        AuthTokenRefreshResult result = authService.refreshAccessToken("admin-web", "plain-refresh-token");

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
        Assert.assertEquals(PrincipalTokenStatus.ACTIVE, accessTokenDao.inserted.getStatus());
        Assert.assertEquals(PrincipalTokenStatus.ACTIVE, refreshTokenDao.inserted.getStatus());
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
        refreshToken.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, EntityIdCodec.toDomain(1L)));
        refreshToken.setIssuedAt(new Date(1000L));
        refreshToken.setExpireAt(new Date(System.currentTimeMillis() + 60000L));
        refreshToken.setStatus(PrincipalTokenStatus.ACTIVE);
        refreshTokenDao.current = refreshToken;
        refreshTokenDao.currentToken = "plain-refresh-token";

        AuthTokenRefreshResult result = authService.exchangeOAuth2Token(
                "admin-web", "secret", "refresh_token", null, null, null, "plain-refresh-token");

        Assert.assertEquals(PrincipalTokenStatus.USED, refreshToken.getStatus());
        Assert.assertNotNull(result.getOauthAccessToken());
        Assert.assertEquals(PrincipalTokenStatus.ACTIVE, accessTokenDao.inserted.getStatus());
        Assert.assertEquals(PrincipalTokenStatus.ACTIVE, refreshTokenDao.inserted.getStatus());
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
        Assert.assertEquals(
                Long.valueOf(1L),
                EntityIdCodec.toValue(authService.authenticateSms("13800000000").getId()));

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
        AuthAccessTokenResult accessToken = authService.createAccessToken("1", "tester");
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
        Assert.assertEquals(
                PrincipalTokenStatus.ACTIVE,
                accessToken.getPrincipalAccessToken().getStatus());
        Assert.assertTrue(principalAuthSessionDao.getTouchCount() > 0);
    }

    @Test
    public void shouldClearUserAccessHolderAfterAuthenticatedRequest() throws Exception {
        AuthAccessTokenResult accessToken = authService.createAccessToken("1", "tester");
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
                session.setLastAccessTime(accessTime);
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
