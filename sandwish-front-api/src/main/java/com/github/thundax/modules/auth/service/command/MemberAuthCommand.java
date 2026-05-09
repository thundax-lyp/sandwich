package com.github.thundax.modules.auth.service.command;

import com.github.thundax.modules.auth.entity.enums.PrincipalAuthenticationMethod;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberAuthCommand {
    private String account;
    private String plainPassword;
    private String mobile;
    private String refreshToken;
    private String accessToken;
    private String ip;
    private String userAgent;
    private PrincipalAuthenticationMethod authenticationMethod;
    private PrincipalIdentityType identityType;
    private String reason;
}
