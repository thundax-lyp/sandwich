package com.github.thundax.modules.member.security.exception;

import org.apache.shiro.authc.AuthenticationException;

/** @author wdit */
public class DisabledUserException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public DisabledUserException() {
        super("msg:用户已禁用，请联系管理员。");
    }
}
