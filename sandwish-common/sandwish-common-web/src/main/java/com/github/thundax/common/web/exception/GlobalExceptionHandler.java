package com.github.thundax.common.web.exception;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.ErrorCode;
import com.github.thundax.common.exception.SandwishException;
import com.github.thundax.common.web.response.ApiResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ApiResponse<Object> handleApiException(ApiException exception) {
        return ApiResponse.failure(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(SandwishException.class)
    public ApiResponse<Object> handleSandwishException(SandwishException exception) {
        return ApiResponse.failure(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return ApiResponse.failure(
                ErrorCode.BAD_REQUEST.getCode(),
                firstFieldErrorMessage(exception.getBindingResult().getFieldError()));
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Object> handleBindException(BindException exception) {
        return ApiResponse.failure(ErrorCode.BAD_REQUEST.getCode(), firstFieldErrorMessage(exception.getFieldError()));
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Object> handleException(Exception exception) {
        return ApiResponse.failure(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage());
    }

    private String firstFieldErrorMessage(FieldError fieldError) {
        if (fieldError == null) {
            return ErrorCode.BAD_REQUEST.getMessage();
        }
        return fieldError.getDefaultMessage();
    }
}
