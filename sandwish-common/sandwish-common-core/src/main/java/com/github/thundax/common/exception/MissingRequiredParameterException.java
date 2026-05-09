package com.github.thundax.common.exception;

public class MissingRequiredParameterException extends ApiException {

    public MissingRequiredParameterException(String name) {
        super("missing required parameter: " + name);
    }
}
