package com.github.thundax.modules.auth.service.query;

import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalIdentityId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalIdentityQuery {
    private PrincipalIdentityId id;
    private PrincipalIdentityType identityType;
    private String identityValue;
    private PrincipalKey principalKey;
    private PrincipalIdentityStatus status;
}
