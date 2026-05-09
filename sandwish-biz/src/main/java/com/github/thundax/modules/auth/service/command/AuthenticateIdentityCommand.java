package com.github.thundax.modules.auth.service.command;

import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticateIdentityCommand {
    private PrincipalIdentityType identityType;
    private String identityValue;
}
