package com.github.thundax.modules.member.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.enums.MemberIdentityStatus;
import com.github.thundax.modules.member.entity.enums.MemberIdentityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberIdentity {
    private EntityId id;
    private EntityId memberId;
    private MemberIdentityType identityType;
    private String identityValue;
    private MemberIdentityStatus status = MemberIdentityStatus.ENABLED;

    public boolean isEnabled() {
        return MemberIdentityStatus.ENABLED == status;
    }

    public boolean isDisabled() {
        return MemberIdentityStatus.DISABLED == status;
    }

    public boolean isAccount() {
        return MemberIdentityType.ACCOUNT == identityType;
    }

    public boolean isMobile() {
        return MemberIdentityType.MOBILE == identityType;
    }

    public boolean isEmail() {
        return MemberIdentityType.EMAIL == identityType;
    }

    public boolean matches(String value) {
        return StringUtils.equals(identityValue, value);
    }

    public void enable() {
        this.status = MemberIdentityStatus.ENABLED;
    }

    public void disable() {
        this.status = MemberIdentityStatus.DISABLED;
    }
}
