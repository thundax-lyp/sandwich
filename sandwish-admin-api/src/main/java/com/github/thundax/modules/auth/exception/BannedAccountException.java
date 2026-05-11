package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.web.exception.SandwishException;
import com.github.thundax.common.web.exception.WebErrorCode;

public class BannedAccountException extends SandwishException {

    public BannedAccountException() {
        super(WebErrorCode.FORBIDDEN, "AUTH-00004", "auth.exception.banned-account", "用户已禁用");
    }
}
