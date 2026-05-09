package com.github.thundax.common.exception;

import com.github.thundax.common.id.BaseLongId;

public class NullBeanException extends ApiException {

    public NullBeanException(String name, BaseLongId id) {
        super(name + " does not exist, id: " + id);
    }
}
