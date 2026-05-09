package com.github.thundax.common.exception;

public class InvalidTokenException extends ApiException {

    public InvalidTokenException() {
        super("invalid token");
    }
}
