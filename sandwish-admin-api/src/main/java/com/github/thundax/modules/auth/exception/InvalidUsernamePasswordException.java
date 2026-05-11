package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;

public class InvalidUsernamePasswordException extends ApiException {

    public InvalidUsernamePasswordException() {
        super("auth.exception.invalid-username-password", "用户名或密码错误");
    }
}
