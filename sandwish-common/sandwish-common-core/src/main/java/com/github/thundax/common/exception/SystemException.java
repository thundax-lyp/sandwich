package com.github.thundax.common.exception;

public class SystemException extends SandwishException {

    public SystemException() {
        super(ErrorCode.SYSTEM_ERROR);
    }

    public SystemException(String message) {
        super(ErrorCode.SYSTEM_ERROR, message);
    }

    public SystemException(String message, Throwable cause) {
        super(ErrorCode.SYSTEM_ERROR, message, cause);
    }
}
