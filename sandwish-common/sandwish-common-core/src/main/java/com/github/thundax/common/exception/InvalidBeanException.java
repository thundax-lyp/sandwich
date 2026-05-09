package com.github.thundax.common.exception;

public class InvalidBeanException extends ApiException {

    public InvalidBeanException(String name, String id) {
        super("invalid " + name + ", id: " + id);
    }
}
