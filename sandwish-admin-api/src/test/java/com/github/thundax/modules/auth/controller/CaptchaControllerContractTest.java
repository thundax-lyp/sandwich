package com.github.thundax.modules.auth.controller;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.modules.auth.service.AuthService;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class CaptchaControllerContractTest {

    private final ApiResponseBodyAdvice advice = new ApiResponseBodyAdvice();

    @Test
    public void shouldWrapRefreshCaptchaJsonResponseWithApiResponseAdvice() throws Exception {
        AuthService authService = mock(AuthService.class);

        mockMvc(authService)
                .perform(post("/api/auth/captcha/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginToken\":\"login-token-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.message").value(ApiResponse.SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.refreshed").value(true));
    }

    @Test
    public void shouldKeepCaptchaImageResponseUnwrapped() throws Exception {
        AuthService authService = mock(AuthService.class);
        when(authService.getCaptcha("login-token-1")).thenReturn("1234");

        mockMvc(authService)
                .perform(get("/api/auth/captcha")
                        .param("loginToken", "login-token-1")
                        .param("width", "140")
                        .param("height", "48"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                .andExpect(content().string(not(startsWith("{\"code\""))));
    }

    private MockMvc mockMvc(AuthService authService) {
        return MockMvcBuilders.standaloneSetup(new CaptchaController(authService))
                .setControllerAdvice(advice)
                .build();
    }
}
