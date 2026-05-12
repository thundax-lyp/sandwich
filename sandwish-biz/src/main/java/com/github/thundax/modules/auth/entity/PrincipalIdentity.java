package com.github.thundax.modules.auth.entity;

import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalIdentityId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalIdentity {
    private PrincipalIdentityId id;
    private PrincipalKey principalKey;
    private PrincipalIdentityType type;
    private String identityValue;
    private PrincipalIdentityStatus status = PrincipalIdentityStatus.ENABLED;

    public boolean isEnabled() {
        return PrincipalIdentityStatus.ENABLED == status;
    }

    public boolean isDisabled() {
        return PrincipalIdentityStatus.DISABLED == status;
    }

    public boolean matches(String value) {
        return StringUtils.equals(identityValue, value);
    }

    public boolean isAccount() {
        return type != null && type.isAccount();
    }

    public boolean isMobile() {
        return type != null && type.isMobile();
    }

    public boolean isEmail() {
        return type != null && type.isEmail();
    }

    public void enable() {
        this.status = PrincipalIdentityStatus.ENABLED;
    }

    public void disable() {
        this.status = PrincipalIdentityStatus.DISABLED;
    }
}
