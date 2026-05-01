package com.github.thundax.modules.sys.handler;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.PermissionDeniedException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(value = PermissionDeniedException.class)
    public Object permissionDeniedExceptionHandle(PermissionDeniedException e) {
        return response(HttpStatus.FORBIDDEN.value(), e.getMessage());
    }

    @ExceptionHandler(value = ApiException.class)
    public Object apiExceptionHandle(ApiException e) {
        return response(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Object methodArgumentNotValidExceptionHandle(MethodArgumentNotValidException e) {
        return response(
                HttpStatus.BAD_REQUEST.value(),
                firstFieldErrorMessage(e.getBindingResult().getFieldError()));
    }

    @ExceptionHandler(value = BindException.class)
    public Object bindExceptionHandle(BindException e) {
        return response(HttpStatus.BAD_REQUEST.value(), firstFieldErrorMessage(e.getFieldError()));
    }

    @ExceptionHandler(value = Exception.class)
    public Object exceptionHandle(Exception e) {
        e.printStackTrace();

        int code;
        String message = e.getMessage();

        code = HttpStatus.INTERNAL_SERVER_ERROR.value();

        return response(code, message);
    }

    private String firstFieldErrorMessage(FieldError fieldError) {
        if (fieldError == null) {
            return "参数错误";
        }
        return fieldError.getDefaultMessage();
    }

    private Map<String, Object> response(int code, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        return map;
    }
}
