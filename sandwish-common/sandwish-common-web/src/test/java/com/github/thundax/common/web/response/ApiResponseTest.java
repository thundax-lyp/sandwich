package com.github.thundax.common.web.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ApiResponseTest {

    @Test
    public void shouldCreateSuccessResponseWithoutData() {
        ApiResponse<Object> response = ApiResponse.success();

        assertEquals(ApiResponse.SUCCESS_CODE, response.getCode());
        assertEquals(ApiResponse.SUCCESS_MESSAGE, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    public void shouldCreateSuccessResponseWithData() {
        ApiResponse<String> response = ApiResponse.success("payload");

        assertEquals(ApiResponse.SUCCESS_CODE, response.getCode());
        assertEquals(ApiResponse.SUCCESS_MESSAGE, response.getMessage());
        assertEquals("payload", response.getData());
    }

    @Test
    public void shouldCreateSuccessResponseWithCustomMessageAndData() {
        ApiResponse<String> response = ApiResponse.success("保存成功", "payload");

        assertEquals(ApiResponse.SUCCESS_CODE, response.getCode());
        assertEquals("保存成功", response.getMessage());
        assertEquals("payload", response.getData());
    }

    @Test
    public void shouldCreateFailureResponseWithDefaultMessage() {
        ApiResponse<Object> response = ApiResponse.failure();

        assertEquals(ApiResponse.ERROR_CODE, response.getCode());
        assertEquals(ApiResponse.ERROR_MESSAGE, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    public void shouldCreateFailureResponseWithCustomCodeAndMessage() {
        ApiResponse<Object> response = ApiResponse.failure("COMMON-00001", "参数错误");

        assertEquals("COMMON-00001", response.getCode());
        assertEquals("参数错误", response.getMessage());
        assertNull(response.getData());
    }
}
