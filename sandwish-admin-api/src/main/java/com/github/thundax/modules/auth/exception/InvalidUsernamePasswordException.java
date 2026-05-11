package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.web.exception.SandwishException;
import com.github.thundax.common.web.exception.WebErrorCode;

public class InvalidUsernamePasswordException extends SandwishException {

    public InvalidUsernamePasswordException() {
        super(WebErrorCode.BAD_REQUEST, "AUTH-00002", "auth.exception.invalid-username-password", "用户名或密码错误");
    }
}
