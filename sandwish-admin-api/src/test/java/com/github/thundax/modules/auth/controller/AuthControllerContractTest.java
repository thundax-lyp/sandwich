package com.github.thundax.modules.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.common.utils.encrypt.Sm2Helper;
import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.service.AdminAuthService;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.command.AdminAuthCommand;
import com.github.thundax.modules.auth.service.command.CreatePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.query.PreAuthSessionQuery;
import com.github.thundax.modules.auth.service.result.AuthAccessTokenResult;
import com.github.thundax.modules.sys.entity.User;
import org.junit.After;
import org.junit.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class AuthControllerContractTest {

    private final ApiResponseBodyAdvice advice = new ApiResponseBodyAdvice();

    @After
    public void tearDown() {
        SpringContextHolder.clearHolder();
    }

    @Test
    public void shouldWrapPreAuthSessionJsonResponseWithApiResponseAdvice() throws Exception {
        AdminAuthService authService = mock(AdminAuthService.class);
        PreAuthSessionService preAuthSessionService = mock(PreAuthSessionService.class);
        PreAuthSession session = preAuthSession();
        when(preAuthSessionService.create(any(CreatePreAuthSessionCommand.class))).thenReturn(session);
        when(preAuthSessionService.get(any(PreAuthSessionQuery.class))).thenReturn(session);

        mockMvc(authService, preAuthSessionService)
                .perform(post("/api/auth/pre-auth-session").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.message").value(ApiResponse.SUCCESS_MESSAGE))
                .andExpect(
                        jsonPath("$.data.loginToken").value(session.getToken().asString()))
                .andExpect(jsonPath("$.data.refreshToken")
                        .value(session.getRefreshToken().asString()))
                .andExpect(jsonPath("$.data.publicKey").value("public-key-1"));
    }

    @Test
    public void shouldWrapLoginJsonResponseWithApiResponseAdvice() throws Exception {
        mockSysLogTemplate();
        Sm2Helper.StringKeyPair keyPair = Sm2Helper.generateKeyPair();
        String encryptedPassword = Sm2Helper.encrypt("plain-password", keyPair.getPublicKey());
        AdminAuthService authService = mock(AdminAuthService.class);
        PreAuthSessionService preAuthSessionService = mock(PreAuthSessionService.class);
        PreAuthSessionId sessionId = PreAuthSessionId.of("session-1");
        when(preAuthSessionService.getIdByToken(any(PreAuthSessionQuery.class))).thenReturn(sessionId);
        when(preAuthSessionService.getValue(any(PreAuthSessionQuery.class)))
                .thenReturn("1234", keyPair.getPrivateKey());
        when(authService.authenticatePassword(any(AdminAuthCommand.class))).thenReturn(user());
        when(authService.createAccessToken(any(AdminAuthCommand.class))).thenReturn(accessToken("access-token-1"));

        mockMvc(authService, preAuthSessionService)
                .perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("user-agent", "JUnit")
                        .content("{\"loginToken\":\"login-token-1\",\"userName\":\"admin\"," + "\"password\":\""
                                + encryptedPassword + "\",\"captcha\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.message").value(ApiResponse.SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.token").value("access-token-1"));
    }

    private MockMvc mockMvc(AdminAuthService authService, PreAuthSessionService preAuthSessionService) {
        return MockMvcBuilders.standaloneSetup(new AuthController(authService, preAuthSessionService, authProperties()))
                .setControllerAdvice(advice)
                .build();
    }

    private AuthProperties authProperties() {
        AuthProperties properties = new AuthProperties();
        properties.setLoginExpiredSeconds(300);
        properties.setMaxLoginCount(100);
        properties.setMaxOnlineCount(100);
        return properties;
    }

    private PreAuthSession preAuthSession() {
        PreAuthSession session = PreAuthSession.create(300);
        session.upsertValue("publicKey", "public-key-1", System.currentTimeMillis() + 300000L);
        return session;
    }

    private User user() {
        User user = new User();
        user.setId(EntityId.of(1L));
        return user;
    }

    private AuthAccessTokenResult accessToken(String token) {
        PrincipalAccessToken accessToken = new PrincipalAccessToken();
        accessToken.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, EntityId.of(1L)));
        return new AuthAccessTokenResult(token, "refresh-token-1", accessToken);
    }

    private void mockSysLogTemplate() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(eq(AmqpTemplate.class))).thenReturn(mock(AmqpTemplate.class));
        SpringContextHolder.setApplicationContext(applicationContext);
    }
}
