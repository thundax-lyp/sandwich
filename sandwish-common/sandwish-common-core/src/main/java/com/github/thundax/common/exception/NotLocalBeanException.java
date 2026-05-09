package com.github.thundax.common.exception;

public class NotLocalBeanException extends ApiException {

    public NotLocalBeanException(String name, String id) {
        super(name + " is not local bean, id: " + id);
    }
}
