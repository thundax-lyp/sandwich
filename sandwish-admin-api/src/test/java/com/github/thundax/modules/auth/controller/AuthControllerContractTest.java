package com.github.thundax.modules.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.common.crypto.Sm2Crypto;
import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.service.AdminAuthService;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.command.AdminAuthCommand;
import com.github.thundax.modules.auth.service.command.CreatePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.query.PreAuthSessionValueQuery;
import com.github.thundax.modules.auth.service.result.AuthAccessTokenResult;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.SysLogMessageService;
import java.util.Date;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class AuthControllerContractTest {

    private final ApiResponseBodyAdvice advice = new ApiResponseBodyAdvice();

    @Test
    public void shouldWrapPreAuthSessionJsonResponseWithApiResponseAdvice() throws Exception {
        AdminAuthService authService = mock(AdminAuthService.class);
        PreAuthSessionService preAuthSessionService = mock(PreAuthSessionService.class);
        PreAuthSession session = preAuthSession();
        when(preAuthSessionService.create(any(CreatePreAuthSessionCommand.class)))
                .thenReturn(session);
        when(preAuthSessionService.get(any(PreAuthSessionId.class))).thenReturn(session);

        mockMvc(authService, preAuthSessionService)
                .perform(post("/api/auth/session/pre-auth-session").contentType(MediaType.APPLICATION_JSON))
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
        Sm2Crypto.StringKeyPair keyPair = Sm2Crypto.generateKeyPair();
        String encryptedPassword = Sm2Crypto.encrypt("plain-password", keyPair.getPublicKey());
        AdminAuthService authService = mock(AdminAuthService.class);
        PreAuthSessionService preAuthSessionService = mock(PreAuthSessionService.class);
        PreAuthSessionId sessionId = PreAuthSessionId.of("session-1");
        when(preAuthSessionService.getIdByToken(any(PreAuthSessionToken.class))).thenReturn(sessionId);
        when(preAuthSessionService.getValue(any(PreAuthSessionValueQuery.class)))
                .thenReturn("1234", keyPair.getPrivateKey());
        when(authService.authenticatePassword(any(AdminAuthCommand.class))).thenReturn(user());
        when(authService.createAccessToken(any(AdminAuthCommand.class))).thenReturn(accessToken("access-token-1"));

        mockMvc(authService, preAuthSessionService)
                .perform(post("/api/auth/session/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("user-agent", "JUnit")
                        .content("{\"loginToken\":\"login-token-1\",\"userName\":\"admin\"," + "\"password\":\""
                                + encryptedPassword + "\",\"captcha\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.message").value(ApiResponse.SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.token").value("access-token-1"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-1"))
                .andExpect(jsonPath("$.data.expireAt").value(1778513052155L));
    }

    @Test
    public void shouldRejectSmsLoginWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc(mock(AdminAuthService.class), mock(PreAuthSessionService.class))
                .perform(post("/api/auth/session/login/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private MockMvc mockMvc(AdminAuthService authService, PreAuthSessionService preAuthSessionService) {
        return MockMvcBuilders.standaloneSetup(new AuthController(
                        authService,
                        preAuthSessionService,
                        authProperties(),
                        mock(SysLogMessageService.class),
                        new ObjectMapper()))
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
        user.setId(UserIdCodec.toDomain(1L));
        return user;
    }

    private AuthAccessTokenResult accessToken(String token) {
        PrincipalAccessToken accessToken = new PrincipalAccessToken();
        accessToken.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, 1L));
        accessToken.setExpireAt(new Date(1778513052155L));
        return new AuthAccessTokenResult(token, "refresh-token-1", accessToken);
    }
}
