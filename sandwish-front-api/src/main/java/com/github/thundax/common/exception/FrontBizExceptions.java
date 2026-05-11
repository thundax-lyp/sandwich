package com.github.thundax.common.exception;

public final class FrontBizExceptions {

    private FrontBizExceptions() {}

    public static BizException invalidCaptcha() {
        return biz("AUTH-00001", "auth.exception.invalid-captcha", "图形验证码错误");
    }

    public static BizException invalidSmsCode() {
        return biz("AUTH-00002", "auth.exception.invalid-sms-code", "短信验证码错误");
    }

    public static BizException invalidEmailCode() {
        return biz("AUTH-00003", "auth.exception.invalid-email-code", "邮箱验证码错误");
    }

    public static BizException invalidUsernamePassword() {
        return biz("AUTH-00004", "auth.exception.invalid-username-password", "用户名或密码错误");
    }

    public static BizException refreshTokenExpired() {
        return biz("AUTH-00005", "auth.exception.refresh-token-expired", "refreshToken 已失效");
    }

    public static BizException accessTokenExpired() {
        return biz("AUTH-00006", "auth.exception.access-token-expired", "accessToken 已失效");
    }

    public static BizException loginFormKeyExpired() {
        return biz("AUTH-00008", "auth.exception.login-form-key-expired", "登录表单密钥已失效");
    }

    public static BizException loginFormExpired() {
        return biz("AUTH-00009", "auth.exception.login-form-expired", "登录表单已失效");
    }

    public static BizException memberUnavailable() {
        return biz("AUTH-00010", "auth.exception.member-unavailable", "会员状态不可用");
    }

    public static BizException identityExists() {
        return biz("AUTH-00011", "auth.exception.identity-exists", "会员标识已存在");
    }

    public static BizException invalidParameter(String field) {
        return new BizException("SYS-00001", "sys.exception.invalid-parameter", field + "不能为空", field);
    }

    private static BizException biz(String code, String messageKey, String defaultMessage) {
        return new BizException(code, messageKey, defaultMessage);
    }
}
