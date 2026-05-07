package com.github.thundax.modules.auth.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.enums.MemberRefreshTokenStatus;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRefreshToken implements Auditable {
    private EntityId id;
    private String tokenId;
    private String tokenHash;
    private String accessTokenId;
    private String sessionId;
    private EntityId memberId;
    private Date issuedAt;
    private Date expireAt;
    private MemberRefreshTokenStatus status = MemberRefreshTokenStatus.ACTIVE;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public boolean isActive() {
        return MemberRefreshTokenStatus.ACTIVE == status;
    }

    public boolean isExpired(Date now) {
        return expireAt != null && now != null && !expireAt.after(now);
    }

    public boolean canRefresh(Date now) {
        return isActive() && !isExpired(now);
    }

    public void markUsed(Date updateTime) {
        this.status = MemberRefreshTokenStatus.USED;
        this.updateDate = updateTime;
    }

    public void revoke(Date updateTime) {
        this.status = MemberRefreshTokenStatus.REVOKED;
        this.updateDate = updateTime;
    }

    public void expire(Date updateTime) {
        this.status = MemberRefreshTokenStatus.EXPIRED;
        this.updateDate = updateTime;
    }
}
