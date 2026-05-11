package com.github.thundax.common.web.exception;

public class ConflictException extends SandwishException {

    public ConflictException() {
        super(WebErrorCode.CONFLICT);
    }

    public ConflictException(String message) {
        super(WebErrorCode.CONFLICT, message);
    }
}
