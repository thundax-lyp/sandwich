package com.github.thundax.common.exception;

import com.github.thundax.common.web.exception.SandwishException;
import com.github.thundax.common.web.exception.WebErrorCode;

public final class FrontResponseExceptions {

    private FrontResponseExceptions() {}

    public static SandwishException invalidCaptcha() {
        return response("AUTH-00001", "auth.exception.invalid-captcha", "图形验证码错误");
    }

    public static SandwishException invalidSmsCode() {
        return response("AUTH-00002", "auth.exception.invalid-sms-code", "短信验证码错误");
    }

    public static SandwishException loginRequestTooMany() {
        return response("AUTH-00007", "auth.exception.login-request-too-many", "登录请求过多");
    }

    public static SandwishException loginFormKeyExpired() {
        return response("AUTH-00008", "auth.exception.login-form-key-expired", "登录表单密钥已失效");
    }

    public static SandwishException loginFormExpired() {
        return response("AUTH-00009", "auth.exception.login-form-expired", "登录表单已失效");
    }

    private static SandwishException response(String code, String messageKey, String defaultMessage) {
        return new SandwishException(WebErrorCode.BAD_REQUEST, code, messageKey, defaultMessage);
    }
}
