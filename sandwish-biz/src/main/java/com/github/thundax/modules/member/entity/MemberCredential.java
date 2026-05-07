package com.github.thundax.modules.member.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.enums.MemberCredentialStatus;
import com.github.thundax.modules.member.entity.enums.MemberCredentialType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberCredential {
    private EntityId id;
    private EntityId memberId;
    private EntityId identityId;
    private MemberCredentialType credentialType;
    private String credentialValue;
    private MemberCredentialStatus status = MemberCredentialStatus.ACTIVE;
    private boolean needChangePassword;
    private int failedCount;
    private int failedLimit;
    private Date lockedUntil;
    private Date expiresAt;
    private Date lastVerifiedAt;

    public boolean isActive() {
        return MemberCredentialStatus.ACTIVE == status;
    }

    public boolean isLocked(Date now) {
        if (MemberCredentialStatus.LOCKED == status) {
            return lockedUntil == null || now == null || lockedUntil.after(now);
        }
        return lockedUntil != null && now != null && lockedUntil.after(now);
    }

    public boolean isExpired(Date now) {
        if (MemberCredentialStatus.EXPIRED == status) {
            return true;
        }
        return expiresAt != null && now != null && !expiresAt.after(now);
    }

    public boolean isPassword() {
        return MemberCredentialType.PASSWORD == credentialType;
    }

    public void markVerified(Date verifiedAt) {
        this.status = MemberCredentialStatus.ACTIVE;
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
        this.status = MemberCredentialStatus.LOCKED;
        this.lockedUntil = lockedUntil;
    }

    public void unlock() {
        this.status = MemberCredentialStatus.ACTIVE;
        this.lockedUntil = null;
    }

    public void expire() {
        this.status = MemberCredentialStatus.EXPIRED;
    }

    public void disable() {
        this.status = MemberCredentialStatus.DISABLED;
    }
}
