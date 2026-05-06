package com.github.thundax.modules.auth.controller;

import static org.junit.Assert.assertTrue;
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
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.entity.AuthSession;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.service.AuthService;
import com.github.thundax.modules.auth.service.result.AuthTokenQueryResult;
import com.github.thundax.modules.sys.entity.User;
import java.util.Collections;
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
    public void shouldWrapLoginFormJsonResponseWithApiResponseAdvice() throws Exception {
        AuthService authService = mock(AuthService.class);
        when(authService.createLoginForm()).thenReturn(loginForm());

        mockMvc(authService)
                .perform(post("/api/auth/form").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.message").value(ApiResponse.SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.loginToken").value("login-token-1"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-1"))
                .andExpect(jsonPath("$.data.publicKey").value("public-key-1"));
    }

    @Test
    public void shouldWrapLoginJsonResponseWithApiResponseAdvice() throws Exception {
        mockSysLogTemplate();
        Sm2Helper.StringKeyPair keyPair = Sm2Helper.generateKeyPair();
        String encryptedPassword = Sm2Helper.encrypt("plain-password", keyPair.getPublicKey());
        AuthService authService = mock(AuthService.class);
        when(authService.validateCaptcha("login-token-1", "1234")).thenReturn(true);
        when(authService.getPrivateKey("login-token-1")).thenReturn(keyPair.getPrivateKey());
        when(authService.authenticatePassword("admin", "plain-password")).thenReturn(user());
        when(authService.createAccessToken("user-1", "admin")).thenReturn(accessToken("access-token-1", "mismatch"));

        mockMvc(authService)
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

    @Test
    public void shouldWrapLogoutJsonResponseWithApiResponseAdvice() throws Exception {
        AuthService authService = mock(AuthService.class);
        when(authService.getAccessToken("access-token-1")).thenReturn(accessToken("access-token-1", "mismatch"));

        mockMvc(authService)
                .perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("user-agent", "JUnit")
                        .content("{\"token\":\"access-token-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.message").value(ApiResponse.SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    public void shouldWrapTokenVerifyJsonResponseWithApiResponseAdvice() throws Exception {
        AuthService authService = mock(AuthService.class);
        when(authService.queryToken("access-token-1"))
                .thenReturn(AuthTokenQueryResult.active("access-token-1", (AuthSession) null, user(), "admin"));

        mockMvc(authService)
                .perform(post("/api/auth/token/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"access-token-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.message").value(ApiResponse.SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    public void shouldDeclareWrappedApiController() {
        assertTrue(AuthController.class.isAnnotationPresent(WrappedApiController.class));
    }

    private MockMvc mockMvc(AuthService authService) {
        return MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(advice)
                .build();
    }

    private LoginForm loginForm() {
        LoginForm form = new LoginForm();
        form.setLoginToken("login-token-1");
        form.setRefreshTokenList(Collections.singletonList("refresh-token-1"));
        form.setExpiredSeconds(300);
        form.setPublicKey("public-key-1");
        return form;
    }

    private User user() {
        User user = new User();
        user.setId(EntityId.of("user-1"));
        return user;
    }

    private AccessToken accessToken(String token, String checkCode) {
        AccessToken accessToken = new AccessToken();
        accessToken.setToken(token);
        accessToken.setUserId("user-1");
        accessToken.setCheckCode(checkCode);
        return accessToken;
    }

    private void mockSysLogTemplate() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(eq(AmqpTemplate.class))).thenReturn(mock(AmqpTemplate.class));
        SpringContextHolder.setApplicationContext(applicationContext);
    }
}
