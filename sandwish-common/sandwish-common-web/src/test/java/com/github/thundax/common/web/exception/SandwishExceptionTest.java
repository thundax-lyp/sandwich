package com.github.thundax.common.web.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class SandwishExceptionTest {

    private final SandwishException exception;
    private final WebErrorCode errorCode;

    public SandwishExceptionTest(SandwishException exception, WebErrorCode errorCode) {
        this.exception = exception;
        this.errorCode = errorCode;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
            {new BadRequestException(), WebErrorCode.BAD_REQUEST},
            {new UnauthorizedException(), WebErrorCode.UNAUTHORIZED},
            {new ForbiddenException(), WebErrorCode.FORBIDDEN},
            {new NotFoundException(), WebErrorCode.NOT_FOUND},
            {new ConflictException(), WebErrorCode.CONFLICT},
            {new SystemException(), WebErrorCode.SYSTEM_ERROR}
        });
    }

    @Test
    public void shouldExposeErrorCodeAndDefaultMessage() {
        assertSame(errorCode, exception.getErrorCode());
        assertEquals(errorCode.getCode(), exception.getCode());
        assertEquals(errorCode.getHttpStatus(), exception.getHttpStatus());
        assertEquals(errorCode.getMessageKey(), exception.getMessageKey());
        assertEquals(errorCode.getMessage(), exception.getMessage());
        assertEquals(errorCode.getMessage(), exception.getDefaultMessage());
    }

    @Test
    public void shouldKeepCustomMessage() {
        SandwishException custom = new SandwishException(errorCode, "custom");

        assertSame(errorCode, custom.getErrorCode());
        assertEquals(errorCode.getCode(), custom.getCode());
        assertEquals(errorCode.getHttpStatus(), custom.getHttpStatus());
        assertEquals("custom", custom.getMessage());
        assertEquals(errorCode.getMessageKey(), custom.getMessageKey());
        assertEquals("custom", custom.getDefaultMessage());
    }
}
