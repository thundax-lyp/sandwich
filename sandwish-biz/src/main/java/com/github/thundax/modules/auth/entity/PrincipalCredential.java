package com.github.thundax.modules.auth.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalCredential {
    private EntityId id;
    private PrincipalKey principalKey;
    private EntityId identityId;
    private PrincipalCredentialType credentialType;
    private String credentialValue;
    private PrincipalCredentialStatus status = PrincipalCredentialStatus.ACTIVE;
    private boolean needChangePassword;
    private int failedCount;
    private int failedLimit;
    private Date lockedUntil;
    private Date expiresAt;
    private Date lastVerifiedAt;

    public boolean isPassword() {
        return credentialType != null && credentialType.isPassword();
    }

    public boolean isActive() {
        return PrincipalCredentialStatus.ACTIVE == status;
    }
}
