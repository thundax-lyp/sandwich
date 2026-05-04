package com.github.thundax.modules.auth.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.enums.OAuthRefreshTokenStatus;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OAuth2 refresh token 事实。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthRefreshToken implements Auditable {
    private EntityId id;
    private String tokenId;
    private String tokenHash;
    private String accessTokenId;
    private String clientId;
    private String tenantId;
    private EntityId userId;
    private Date issuedAt;
    private Date expireAt;
    private OAuthRefreshTokenStatus status = OAuthRefreshTokenStatus.ACTIVE;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public boolean isActive() {
        return OAuthRefreshTokenStatus.ACTIVE == status;
    }

    public boolean isExpired(Date now) {
        return expireAt != null && now != null && !expireAt.after(now);
    }

    public boolean canRefresh(Date now) {
        return isActive() && !isExpired(now);
    }

    public void markUsed(Date updateTime) {
        this.status = OAuthRefreshTokenStatus.USED;
        this.updateDate = updateTime;
    }

    public void revoke(Date updateTime) {
        this.status = OAuthRefreshTokenStatus.REVOKED;
        this.updateDate = updateTime;
    }

    public void expire(Date updateTime) {
        this.status = OAuthRefreshTokenStatus.EXPIRED;
        this.updateDate = updateTime;
    }
}
