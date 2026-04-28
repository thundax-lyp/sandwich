package com.github.thundax.modules.sys.handler;

import com.github.thundax.common.exception.PermissionDeniedException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public Object exceptionHandle(Exception e) {
        e.printStackTrace();

        int code;
        String message = e.getMessage();

        if (e instanceof PermissionDeniedException) {
            code = HttpStatus.FORBIDDEN.value();

        } else {
            code = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        return map;
    }
}
