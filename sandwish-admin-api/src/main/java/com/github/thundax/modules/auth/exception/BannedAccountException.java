package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;

public class BannedAccountException extends ApiException {

    public BannedAccountException() {
        super("auth.exception.banned-account", "用户已禁用");
    }
}
