package com.github.thundax.modules.auth.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.enums.MemberAuthSessionStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
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
public class MemberAuthSession {
    private EntityId id;
    private PrincipalKey principalKey;
    private EntityId identityId;
    private PrincipalIdentityType identityType;
    private String loginType;
    private MemberAuthSessionStatus status = MemberAuthSessionStatus.ACTIVE;
    private Date issuedAt;
    private Date lastAccessTime;
    private Date expireAt;
    private Date logoutAt;
    private String invalidateReason;

    public boolean isActive() {
        return MemberAuthSessionStatus.ACTIVE == status;
    }

    public boolean isExpired(Date now) {
        return expireAt != null && now != null && !expireAt.after(now);
    }

    public int remainingSeconds(Date now) {
        if (expireAt == null || now == null) {
            return 0;
        }
        long remainingMillis = expireAt.getTime() - now.getTime();
        if (remainingMillis <= 0L) {
            return 0;
        }
        return (int) Math.max(1L, remainingMillis / 1000L);
    }

    public void touch(Date accessTime) {
        this.lastAccessTime = accessTime;
    }

    public void logout(Date logoutAt) {
        this.status = MemberAuthSessionStatus.LOGGED_OUT;
        this.logoutAt = logoutAt;
    }

    public void invalidate(String reason) {
        this.status = MemberAuthSessionStatus.INVALIDATED;
        this.invalidateReason = reason;
    }

    public void expire() {
        this.status = MemberAuthSessionStatus.EXPIRED;
    }
}
