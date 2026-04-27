package com.github.thundax.common.web;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/** @author wdit */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public Object exceptionHandle(Exception e) {
        e.printStackTrace();

        Map<String, Object> map = new HashMap<>();
        map.put("code", 500);
        map.put("message", "error");
        return map;
    }
}
