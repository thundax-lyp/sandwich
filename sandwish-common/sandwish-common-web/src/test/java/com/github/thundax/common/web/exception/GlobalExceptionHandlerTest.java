package com.github.thundax.common.web.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.github.thundax.common.web.response.ApiResponse;
import org.junit.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    public void shouldConvertSandwishExceptionToFailureResponse() {
        ApiResponse<Object> response = handler.handleSandwishException(new BadRequestException("参数缺失"));

        assertEquals(WebErrorCode.BAD_REQUEST.getCode(), response.getCode());
        assertEquals("参数缺失", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    public void shouldConvertBindExceptionToBadRequestResponse() {
        BindException exception = new BindException(new Object(), "request");
        exception.addError(new FieldError("request", "name", "名称不能为空"));

        ApiResponse<Object> response = handler.handleBindException(exception);

        assertEquals(WebErrorCode.BAD_REQUEST.getCode(), response.getCode());
        assertEquals("名称不能为空", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    public void shouldConvertUnknownExceptionToSystemFailureResponse() {
        ApiResponse<Object> response = handler.handleException(new RuntimeException("boom"));

        assertEquals(WebErrorCode.SYSTEM_ERROR.getCode(), response.getCode());
        assertEquals(WebErrorCode.SYSTEM_ERROR.getMessage(), response.getMessage());
        assertNull(response.getData());
    }
}
