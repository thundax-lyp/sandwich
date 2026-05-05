package com.github.thundax.common.exception;

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
    private final ErrorCode errorCode;

    public SandwishExceptionTest(SandwishException exception, ErrorCode errorCode) {
        this.exception = exception;
        this.errorCode = errorCode;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
            {new BadRequestException(), ErrorCode.BAD_REQUEST},
            {new UnauthorizedException(), ErrorCode.UNAUTHORIZED},
            {new ForbiddenException(), ErrorCode.FORBIDDEN},
            {new NotFoundException(), ErrorCode.NOT_FOUND},
            {new ConflictException(), ErrorCode.CONFLICT},
            {new SystemException(), ErrorCode.SYSTEM_ERROR}
        });
    }

    @Test
    public void shouldExposeErrorCodeAndDefaultMessage() {
        assertSame(errorCode, exception.getErrorCode());
        assertEquals(errorCode.getCode(), exception.getCode());
        assertEquals(errorCode.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldKeepCustomMessageAndCause() {
        RuntimeException cause = new RuntimeException("root");
        SandwishException custom = new SandwishException(errorCode, "custom", cause);

        assertSame(errorCode, custom.getErrorCode());
        assertEquals(errorCode.getCode(), custom.getCode());
        assertEquals("custom", custom.getMessage());
        assertSame(cause, custom.getCause());
    }
}
