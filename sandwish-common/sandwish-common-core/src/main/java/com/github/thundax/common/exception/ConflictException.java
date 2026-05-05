package com.github.thundax.common.exception;

public class ConflictException extends SandwishException {

    public ConflictException() {
        super(ErrorCode.CONFLICT);
    }

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }

    public ConflictException(String message, Throwable cause) {
        super(ErrorCode.CONFLICT, message, cause);
    }
}
