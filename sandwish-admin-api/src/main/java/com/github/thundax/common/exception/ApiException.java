package com.github.thundax.common.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends Exception {

    private int code = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public ApiException(String message) {
        super(message);
    }

    public int getCode() {
        return code;
    }
}
