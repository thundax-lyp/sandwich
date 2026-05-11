package com.github.thundax.common.web.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.github.thundax.common.web.i18n.I18nMessageResolver;
import com.github.thundax.common.web.response.ApiResponse;
import java.util.Locale;
import org.junit.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

public class GlobalExceptionHandlerTest {

    private final StaticMessageSource messageSource = new StaticMessageSource();
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(new I18nMessageResolver(messageSource));

    @Test
    public void shouldConvertSandwishExceptionToFailureResponse() {
        messageSource.addMessage(WebErrorCode.BAD_REQUEST.getMessageKey(), Locale.getDefault(), "Bad request");

        ApiResponse<Object> response = handler.handleSandwishException(new BadRequestException("参数缺失"));

        assertEquals(WebErrorCode.BAD_REQUEST.getCode(), response.getCode());
        assertEquals("Bad request", response.getMessage());
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
