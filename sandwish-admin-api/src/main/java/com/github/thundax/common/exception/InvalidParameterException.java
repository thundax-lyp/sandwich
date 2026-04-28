package com.github.thundax.common.exception;

public class InvalidParameterException extends ApiException {

    public InvalidParameterException(String name) {
        super("无效的参数: " + name);
    }
}
