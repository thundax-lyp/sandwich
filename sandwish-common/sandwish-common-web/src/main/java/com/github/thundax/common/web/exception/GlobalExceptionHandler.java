package com.github.thundax.common.web.exception;

import com.github.thundax.common.web.response.ApiResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SandwishException.class)
    public ApiResponse<Object> handleSandwishException(SandwishException exception) {
        return ApiResponse.failure(exception.getCode(), exception.getMessage());
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
        return ApiResponse.failure(WebErrorCode.SYSTEM_ERROR.getCode(), WebErrorCode.SYSTEM_ERROR.getMessage());
    }

    private String firstFieldErrorMessage(FieldError fieldError) {
        if (fieldError == null) {
            return WebErrorCode.BAD_REQUEST.getMessage();
        }
        return fieldError.getDefaultMessage();
    }
}
