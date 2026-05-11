package com.github.thundax.common.web.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.github.thundax.common.web.i18n.I18nMessageResolver;
import com.github.thundax.common.web.response.ApiResponse;
import java.util.Collections;
import java.util.Locale;
import org.junit.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

public class GlobalExceptionHandlerTest {

    private final StaticMessageSource messageSource = new StaticMessageSource();
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(
            new I18nMessageResolver(messageSource), Collections.singletonList(new DefaultExceptionTranslator()));

    @Test
    public void shouldConvertSandwishExceptionToFailureResponse() {
        messageSource.addMessage(WebErrorCode.BAD_REQUEST.getMessageKey(), Locale.getDefault(), "Bad request");

        ResponseEntity<ApiResponse<Object>> response = handler.handleSandwishException(new BadRequestException("参数缺失"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(WebErrorCode.BAD_REQUEST.getCode(), response.getBody().getCode());
        assertEquals("Bad request", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    public void shouldConvertBindExceptionToBadRequestResponse() {
        BindException exception = new BindException(new Object(), "request");
        exception.addError(new FieldError("request", "name", "名称不能为空"));

        ResponseEntity<ApiResponse<Object>> response = handler.handleException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(WebErrorCode.BAD_REQUEST.getCode(), response.getBody().getCode());
        assertEquals("名称不能为空", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    public void shouldConvertUnknownExceptionToSystemFailureResponse() {
        ResponseEntity<ApiResponse<Object>> response = handler.handleException(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(WebErrorCode.SYSTEM_ERROR.getCode(), response.getBody().getCode());
        assertEquals(WebErrorCode.SYSTEM_ERROR.getMessage(), response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }
}
