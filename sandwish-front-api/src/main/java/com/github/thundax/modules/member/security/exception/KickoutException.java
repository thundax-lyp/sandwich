package com.github.thundax.modules.member.security.exception;

import org.apache.shiro.authc.AuthenticationException;

/**
 * @author wdit
 */
public class KickoutException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public KickoutException() {
        super("msg:账号已在其它地方登录，请重新登录。");
    }

}
