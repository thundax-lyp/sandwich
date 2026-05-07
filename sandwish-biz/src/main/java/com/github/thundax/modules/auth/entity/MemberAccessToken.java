package com.github.thundax.modules.auth.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.enums.MemberAccessTokenStatus;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberAccessToken implements Auditable {
    private EntityId id;
    private String tokenId;
    private String tokenHash;
    private String sessionId;
    private EntityId memberId;
    private Date issuedAt;
    private Date expireAt;
    private MemberAccessTokenStatus status = MemberAccessTokenStatus.ACTIVE;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public boolean isActive() {
        return MemberAccessTokenStatus.ACTIVE == status;
    }

    public boolean isExpired(Date now) {
        return expireAt != null && now != null && !expireAt.after(now);
    }

    public boolean canAccess(Date now) {
        return isActive() && !isExpired(now);
    }

    public void revoke(Date updateTime) {
        this.status = MemberAccessTokenStatus.REVOKED;
        this.updateDate = updateTime;
    }

    public void expire(Date updateTime) {
        this.status = MemberAccessTokenStatus.EXPIRED;
        this.updateDate = updateTime;
    }
}
