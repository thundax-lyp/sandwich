package com.github.thundax.modules.member.security;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * @author wdit
 */
public class MemberAuthenticationToken extends UsernamePasswordToken {

	private static final long serialVersionUID = 1L;

	private String captcha;

	public MemberAuthenticationToken() {
		super();
	}

	public MemberAuthenticationToken(String username, char[] password, boolean rememberMe, String host, String captcha) {
		super(username, password, rememberMe, host);
		this.captcha = captcha;
	}

	public String getCaptcha() {
		return captcha;
	}

	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}

}
