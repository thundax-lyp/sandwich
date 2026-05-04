package com.github.thundax.modules.auth.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.enums.AuthSessionStatus;
import com.github.thundax.modules.auth.entity.enums.UserIdentityType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 后台认证会话。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthSession implements Auditable {
    private EntityId id;
    private String sessionId;
    private String token;
    private EntityId userId;
    private EntityId identityId;
    private UserIdentityType identityType;
    private String loginType;
    private AuthSessionStatus status = AuthSessionStatus.ACTIVE;
    private Date issuedAt;
    private Date lastAccessTime;
    private Date expireAt;
    private Date logoutAt;
    private String invalidateReason;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public boolean isActive() {
        return AuthSessionStatus.ACTIVE == status;
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
        this.status = AuthSessionStatus.LOGGED_OUT;
        this.logoutAt = logoutAt;
    }

    public void invalidate(String reason) {
        this.status = AuthSessionStatus.INVALIDATED;
        this.invalidateReason = reason;
    }

    public void expire() {
        this.status = AuthSessionStatus.EXPIRED;
    }
}
