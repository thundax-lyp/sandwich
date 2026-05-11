package com.github.thundax.common.web.exception;

public class BadRequestException extends SandwishException {

    public BadRequestException() {
        super(WebErrorCode.BAD_REQUEST);
    }

    public BadRequestException(String message) {
        super(WebErrorCode.BAD_REQUEST, message);
    }
}
