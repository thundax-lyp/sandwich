package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.BizException;

public class InvalidCaptchaException extends BizException {

    public InvalidCaptchaException() {
        super("AUTH-00001", "auth.exception.invalid-captcha", "验证码错误");
    }
}
