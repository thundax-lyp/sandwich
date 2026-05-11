package com.github.thundax.common.web.exception;

public interface ExceptionTranslator {

    SandwishException translate(Exception exception);
}
