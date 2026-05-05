package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.enums.UserIdentityStatus;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * 后台用户登录标识。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentity {
    private EntityId id;
    private EntityId userId;
    private UserIdentityType identityType;
    private String identityValue;
    private UserIdentityStatus status = UserIdentityStatus.ENABLED;

    public boolean isEnabled() {
        return UserIdentityStatus.ENABLED == status;
    }

    public boolean isDisabled() {
        return UserIdentityStatus.DISABLED == status;
    }

    public boolean isAccount() {
        return UserIdentityType.ACCOUNT == identityType;
    }

    public boolean matches(String value) {
        return StringUtils.equals(identityValue, value);
    }

    public void enable() {
        this.status = UserIdentityStatus.ENABLED;
    }

    public void disable() {
        this.status = UserIdentityStatus.DISABLED;
    }
}
