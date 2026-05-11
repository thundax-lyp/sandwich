package com.github.thundax.common.exception;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ApiExceptionTest {

    @Test
    public void shouldKeepPlainMessageExceptionCompatible() {
        ApiException exception = new ApiException("plain message");

        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), exception.getCode());
        assertEquals("plain message", exception.getMessage());
        assertNull(exception.getMessageKey());
        assertArrayEquals(new Object[0], exception.getMessageArgs());
    }

    @Test
    public void shouldCarryMessageKeyAndArgsWithoutResolvingText() {
        ApiException exception = new ApiException(400, "auth.exception.invalid-captcha", "验证码错误", "login");

        assertEquals(400, exception.getCode());
        assertEquals("验证码错误", exception.getMessage());
        assertEquals("auth.exception.invalid-captcha", exception.getMessageKey());
        assertArrayEquals(new Object[] {"login"}, exception.getMessageArgs());
    }

    @Test
    public void shouldProtectMessageArgsFromExternalMutation() {
        Object[] args = new Object[] {"before"};
        ApiException exception = new ApiException("auth.exception.invalid-password", "密码错误", args);

        args[0] = "after";
        Object[] copiedArgs = exception.getMessageArgs();
        copiedArgs[0] = "changed";

        assertArrayEquals(new Object[] {"before"}, exception.getMessageArgs());
    }
}
