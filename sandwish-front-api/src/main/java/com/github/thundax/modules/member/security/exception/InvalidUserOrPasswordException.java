package com.github.thundax.modules.member.security.exception;

import org.apache.shiro.authc.AuthenticationException;

/** @author wdit */
public class InvalidUserOrPasswordException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public static final String MESSAGE = "用户名或密码错误。";

    public InvalidUserOrPasswordException() {
        super("msg:" + MESSAGE);
    }
}
