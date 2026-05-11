package com.github.thundax.common.web.exception;

public class UnauthorizedException extends SandwishException {

    public UnauthorizedException() {
        super(WebErrorCode.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(WebErrorCode.UNAUTHORIZED, message);
    }
}
