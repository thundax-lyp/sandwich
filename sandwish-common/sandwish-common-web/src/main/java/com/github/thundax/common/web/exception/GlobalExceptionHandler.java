package com.github.thundax.common.web.exception;

import com.github.thundax.common.web.i18n.I18nMessageResolver;
import com.github.thundax.common.web.response.ApiResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final I18nMessageResolver i18nMessageResolver;

    public GlobalExceptionHandler(I18nMessageResolver i18nMessageResolver) {
        this.i18nMessageResolver = i18nMessageResolver;
    }

    @ExceptionHandler(SandwishException.class)
    public ApiResponse<Object> handleSandwishException(SandwishException exception) {
        return ApiResponse.failure(
                exception.getCode(),
                i18nMessageResolver.resolve(
                        exception.getMessageKey(), exception.getDefaultMessage(), exception.getMessageArgs()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return ApiResponse.failure(
                WebErrorCode.BAD_REQUEST.getCode(),
                firstFieldErrorMessage(exception.getBindingResult().getFieldError()));
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Object> handleBindException(BindException exception) {
        return ApiResponse.failure(
                WebErrorCode.BAD_REQUEST.getCode(), firstFieldErrorMessage(exception.getFieldError()));
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Object> handleException(Exception exception) {
        return ApiResponse.failure(
                WebErrorCode.SYSTEM_ERROR.getCode(),
                i18nMessageResolver.resolve(
                        WebErrorCode.SYSTEM_ERROR.getMessageKey(), WebErrorCode.SYSTEM_ERROR.getMessage()));
    }

    private String firstFieldErrorMessage(FieldError fieldError) {
        if (fieldError == null) {
            return WebErrorCode.BAD_REQUEST.getMessage();
        }
        return fieldError.getDefaultMessage();
    }
}
