package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;

public class InvalidCaptchaException extends ApiException {

    public InvalidCaptchaException() {
        super("auth.exception.invalid-captcha", "验证码错误");
    }
}
