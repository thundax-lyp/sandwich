package com.github.thundax.common.web;

import com.github.thundax.common.collect.MapUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @author wdit
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public Object exceptionHandle(Exception e) {
        e.printStackTrace();

        Map<String, Object> map = MapUtils.newHashMap();
        map.put("code", 500);
        map.put("message", "error");
        return map;
    }

}
