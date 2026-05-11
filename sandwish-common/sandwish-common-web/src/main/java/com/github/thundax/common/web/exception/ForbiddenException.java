package com.github.thundax.common.web.exception;

public class ForbiddenException extends SandwishException {

    public ForbiddenException() {
        super(WebErrorCode.FORBIDDEN);
    }

    public ForbiddenException(String message) {
        super(WebErrorCode.FORBIDDEN, message);
    }
}
