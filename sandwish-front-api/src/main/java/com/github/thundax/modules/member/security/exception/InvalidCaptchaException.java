package com.github.thundax.modules.member.security.exception;

import org.apache.shiro.authc.AuthenticationException;

/**
 * @author wdit
 */
public class InvalidCaptchaException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public InvalidCaptchaException() {
        super("msg:验证码错误。");
    }

}
