package com.github.thundax.modules.auth.controller;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.modules.auth.service.AdminAuthService;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class CaptchaControllerContractTest {

    private final ApiResponseBodyAdvice advice = new ApiResponseBodyAdvice();

    @Test
    public void shouldWrapRefreshCaptchaJsonResponseWithApiResponseAdvice() throws Exception {
        AdminAuthService authService = mock(AdminAuthService.class);

        mockMvc(authService)
                .perform(post("/api/auth/captcha/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginToken\":\"login-token-1\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.message").value(ApiResponse.SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.refreshed").value(true));
    }

    @Test
    public void shouldUseJsonUtf8ForCaptchaErrorResponse() throws Exception {
        AdminAuthService authService = mock(AdminAuthService.class);

        mockMvc(authService)
                .perform(get("/api/auth/captcha"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code").value(-1));
    }

    @Test
    public void shouldKeepCaptchaImageResponseUnwrapped() throws Exception {
        AdminAuthService authService = mock(AdminAuthService.class);
        when(authService.getCaptcha("login-token-1")).thenReturn("1234");

        MvcResult result = mockMvc(authService)
                .perform(get("/api/auth/captcha")
                        .param("loginToken", "login-token-1")
                        .param("width", "150")
                        .param("height", "40"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
                .andExpect(content().string(not(startsWith("{\"code\""))))
                .andReturn();

        byte[] body = result.getResponse().getContentAsByteArray();
        assertTrue(body.length > 8);
        assertEquals(0x89, body[0] & 0xFF);
        assertEquals('P', body[1]);
        assertEquals('N', body[2]);
        assertEquals('G', body[3]);
    }

    private MockMvc mockMvc(AdminAuthService authService) {
        return MockMvcBuilders.standaloneSetup(new CaptchaController(authService))
                .setControllerAdvice(advice)
                .build();
    }
}
