package com.github.thundax.modules.assist.controller;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.modules.assist.controller.request.SignaturePageRequest;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.assist.service.SignatureService;
import com.github.thundax.modules.sys.service.LogService;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import java.lang.reflect.Method;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class SignatureControllerContractTest {

    private final ApiResponseBodyAdvice advice = new ApiResponseBodyAdvice();

    @Test
    public void shouldWrapVerifyJsonResponseWithApiResponseAdvice() throws Exception {
        SignatureService signatureService = mock(SignatureService.class);
        when(signatureService.getByBusiness("log", "biz-1")).thenReturn(null);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller(signatureService))
                .setControllerAdvice(advice)
                .build();

        mockMvc.perform(post("/api/assist/signature/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessType\":\"log\",\"businessId\":\"biz-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.message").value(ApiResponse.SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.verified").value(false));
    }

    @Test
    public void shouldDeclareWrappedApiController() {
        assertTrue(SignatureController.class.isAnnotationPresent(WrappedApiController.class));
    }

    @Test
    public void shouldNotWrapPageResponseAgain() throws Exception {
        PageResponse<Object> body = new PageResponse<>();

        Object result = advice.beforeBodyWrite(
                body,
                returnType("page", SignaturePageRequest.class),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                null,
                null);

        assertSame(body, result);
    }

    private SignatureController controller(SignatureService signatureService) {
        return new SignatureController(
                signatureService,
                mock(SignService.class),
                mock(LogService.class),
                mock(UserService.class),
                mock(MenuService.class),
                mock(RoleService.class));
    }

    private MethodParameter returnType(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = SignatureController.class.getDeclaredMethod(methodName, parameterTypes);
        return new MethodParameter(method, -1);
    }
}
