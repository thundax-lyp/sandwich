package com.github.thundax.common.web;

import com.github.thundax.common.web.response.ApiResponse;
import java.util.HashMap;
import java.util.Map;

public class ReturnObject extends HashMap<String, Object> {

    public ReturnObject() {
        put("code", ApiResponse.SUCCESS_CODE);
        put("message", ApiResponse.SUCCESS_MESSAGE);
    }

    public static ReturnObject error() {
        return error(ApiResponse.ERROR_CODE, ApiResponse.ERROR_MESSAGE);
    }

    public static ReturnObject error(String msg) {
        return error(ApiResponse.ERROR_CODE, msg);
    }

    public static ReturnObject error(String code, String msg) {
        ReturnObject returnObject = new ReturnObject();
        returnObject.put("code", code);
        returnObject.put("message", msg);
        return returnObject;
    }

    public static ReturnObject ok(String msg) {
        ReturnObject returnObject = new ReturnObject();
        returnObject.put("message", msg);
        return returnObject;
    }

    public static ReturnObject ok(Map<String, Object> map) {
        ReturnObject returnObject = new ReturnObject();
        returnObject.putAll(map);
        return returnObject;
    }

    public static ReturnObject ok() {
        return new ReturnObject();
    }

    public ReturnObject put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
