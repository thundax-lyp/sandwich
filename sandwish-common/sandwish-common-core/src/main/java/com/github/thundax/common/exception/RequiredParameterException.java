package com.github.thundax.common.exception;

public class RequiredParameterException extends ApiException {

    public RequiredParameterException(String message) {
        super("parameter " + message + " is required");
    }
}
