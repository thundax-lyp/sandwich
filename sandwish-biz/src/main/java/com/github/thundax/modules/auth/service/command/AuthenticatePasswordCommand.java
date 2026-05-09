package com.github.thundax.modules.auth.service.command;

import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.service.dto.PrincipalPasswordPolicyDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatePasswordCommand {
    private PrincipalIdentityType identityType;
    private String identityValue;
    private PrincipalCredentialType credentialType;
    private String plainPassword;
    private PrincipalPasswordPolicyDTO passwordPolicy;
}
