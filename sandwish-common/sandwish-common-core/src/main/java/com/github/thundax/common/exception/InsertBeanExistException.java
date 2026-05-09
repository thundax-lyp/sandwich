package com.github.thundax.common.exception;

import com.github.thundax.common.id.BaseLongId;

public class InsertBeanExistException extends ApiException {

    public InsertBeanExistException(String name, BaseLongId id) {
        super(name + " object already exists, id: " + id);
    }
}
