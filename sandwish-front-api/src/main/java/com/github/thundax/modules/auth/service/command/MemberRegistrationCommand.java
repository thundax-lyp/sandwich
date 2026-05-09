package com.github.thundax.modules.auth.service.command;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRegistrationCommand {
    private String loginToken;
    private String name;
    private String account;
    private String encryptedPassword;
    private String captcha;
    private String mobile;
    private String validateCode;
    private String email;
}
