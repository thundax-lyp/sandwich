package com.github.thundax.modules.auth.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.enums.UserCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.UserCredentialType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 后台用户认证凭据。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCredential implements Auditable {
    private EntityId id;
    private EntityId userId;
    private EntityId identityId;
    private UserCredentialType credentialType;
    private String credentialValue;
    private UserCredentialStatus status = UserCredentialStatus.ACTIVE;
    private boolean needChangePassword;
    private int failedCount;
    private int failedLimit;
    private Date lockedUntil;
    private Date expiresAt;
    private Date lastVerifiedAt;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public boolean isActive() {
        return UserCredentialStatus.ACTIVE == status;
    }

    public boolean isLocked(Date now) {
        if (UserCredentialStatus.LOCKED == status) {
            return true;
        }
        return lockedUntil != null && now != null && lockedUntil.after(now);
    }

    public boolean isExpired(Date now) {
        if (UserCredentialStatus.EXPIRED == status) {
            return true;
        }
        return expiresAt != null && now != null && !expiresAt.after(now);
    }

    public boolean isPassword() {
        return UserCredentialType.PASSWORD == credentialType;
    }

    public void markVerified(Date verifiedAt) {
        this.status = UserCredentialStatus.ACTIVE;
        this.failedCount = 0;
        this.lockedUntil = null;
        this.lastVerifiedAt = verifiedAt;
    }

    public void markFailed(Date lockedUntil) {
        this.failedCount += 1;
        if (failedLimit > 0 && failedCount >= failedLimit) {
            lock(lockedUntil);
        }
    }

    public void lock(Date lockedUntil) {
        this.status = UserCredentialStatus.LOCKED;
        this.lockedUntil = lockedUntil;
    }

    public void unlock() {
        this.status = UserCredentialStatus.ACTIVE;
        this.lockedUntil = null;
    }

    public void expire() {
        this.status = UserCredentialStatus.EXPIRED;
    }

    public void disable() {
        this.status = UserCredentialStatus.DISABLED;
    }
}
