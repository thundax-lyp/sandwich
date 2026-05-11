package com.github.thundax.common.web.exception;

public class NotFoundException extends SandwishException {

    public NotFoundException() {
        super(WebErrorCode.NOT_FOUND);
    }

    public NotFoundException(String message) {
        super(WebErrorCode.NOT_FOUND, message);
    }
}
