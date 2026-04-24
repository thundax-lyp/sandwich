package com.github.thundax.common.exception;

import org.springframework.http.HttpStatus;

/**
 * @author wdit
 */
public class ApiException extends Exception {

    private static final long serialVersionUID = 1L;

    private int code = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public ApiException() {
        super();
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
