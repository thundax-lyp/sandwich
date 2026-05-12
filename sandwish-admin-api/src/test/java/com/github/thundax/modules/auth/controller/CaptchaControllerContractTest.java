package com.github.thundax.modules.auth.controller;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class CaptchaControllerContractTest {

    private final ApiResponseBodyAdvice advice = new ApiResponseBodyAdvice();

    @Test
    public void shouldKeepCaptchaImageResponseUnwrapped() throws Exception {
        PreAuthSessionService preAuthSessionService = mock(PreAuthSessionService.class);
        PreAuthSessionId sessionId = PreAuthSessionId.of("1");
        when(preAuthSessionService.getIdByToken(any(PreAuthSessionToken.class))).thenReturn(sessionId);
        when(preAuthSessionService.getValue(any(PreAuthSessionId.class), any(String.class)))
                .thenReturn("1234");

        MvcResult result = mockMvc(preAuthSessionService)
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

    private MockMvc mockMvc(PreAuthSessionService preAuthSessionService) {
        return MockMvcBuilders.standaloneSetup(new CaptchaController(preAuthSessionService))
                .setControllerAdvice(advice)
                .build();
    }
}
