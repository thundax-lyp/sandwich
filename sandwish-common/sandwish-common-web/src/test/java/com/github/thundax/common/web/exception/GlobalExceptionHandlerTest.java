package com.github.thundax.common.web.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.github.thundax.common.exception.BadRequestException;
import com.github.thundax.common.exception.ErrorCode;
import com.github.thundax.common.web.response.ApiResponse;
import org.junit.Test;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    public void shouldConvertSandwishExceptionToFailureResponse() {
        ApiResponse<Object> response = handler.handleSandwishException(new BadRequestException("参数缺失"));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getCode());
        assertEquals("参数缺失", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    public void shouldConvertUnknownExceptionToSystemFailureResponse() {
        ApiResponse<Object> response = handler.handleException(new RuntimeException("boom"));

        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), response.getCode());
        assertEquals(ErrorCode.SYSTEM_ERROR.getMessage(), response.getMessage());
        assertNull(response.getData());
    }
}
