package com.github.thundax.modules.auth.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
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
    private EntityId id;
    private PrincipalKey principalKey;
    private PrincipalIdentityType type;
    private String identityValue;

    public boolean matches(String value) {
        return StringUtils.equals(identityValue, value);
    }

    public boolean isAccount() {
        return PrincipalIdentityType.USER_ACCOUNT == type || PrincipalIdentityType.MEMBER_ACCOUNT == type;
    }

    public boolean isMobile() {
        return PrincipalIdentityType.MEMBER_MOBILE == type;
    }

    public boolean isEmail() {
        return PrincipalIdentityType.MEMBER_EMAIL == type;
    }
}
