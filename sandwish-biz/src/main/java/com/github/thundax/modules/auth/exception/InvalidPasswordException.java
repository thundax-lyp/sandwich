package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.BizException;

public class InvalidPasswordException extends BizException {

    public InvalidPasswordException() {
        super("AUTH-00003", "auth.exception.invalid-password", "密码错误");
    }
}
