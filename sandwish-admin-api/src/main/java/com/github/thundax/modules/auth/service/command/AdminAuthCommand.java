package com.github.thundax.modules.auth.service.command;

import com.github.thundax.modules.auth.entity.enums.PrincipalAuthenticationMethod;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.service.result.AuthAccessTokenResult;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.valueobject.UserId;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminAuthCommand {
    private UserId userId;
    private String loginName;
    private String plainPassword;
    private String mobile;
    private String code;
    private String token;
    private String reason;
    private String ip;
    private String userAgent;
    private PrincipalAuthenticationMethod authenticationMethod;
    private PrincipalIdentityType identityType;
    private AuthAccessTokenResult accessToken;
    private User user;
    private String clientId;
    private String clientSecret;
    private String grantType;
    private String redirectUri;
    private String authorizationCode;
    private String codeVerifier;
    private String refreshToken;
    private String state;
    private String codeChallenge;
    private String codeChallengeMethod;
    private List<String> scopes;
    private boolean approved;
}
