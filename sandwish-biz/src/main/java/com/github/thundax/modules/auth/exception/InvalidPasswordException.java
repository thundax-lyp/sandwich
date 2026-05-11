package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;

public class InvalidPasswordException extends ApiException {

    public InvalidPasswordException() {
        super("auth.exception.invalid-password", "密码错误");
    }
}
