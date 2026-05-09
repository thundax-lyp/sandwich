package com.github.thundax.modules.auth.service.query;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalCredentialQuery {
    private EntityId id;
    private EntityId identityId;
    private PrincipalCredentialType credentialType;
    private PrincipalKey principalKey;
    private PrincipalCredentialStatus status;
}
