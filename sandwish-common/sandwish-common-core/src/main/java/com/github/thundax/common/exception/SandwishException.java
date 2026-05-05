package com.github.thundax.common.exception;

public class SandwishException extends RuntimeException {

    private final ErrorCode errorCode;

    public SandwishException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public SandwishException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SandwishException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getCode() {
        return errorCode.getCode();
    }
}
