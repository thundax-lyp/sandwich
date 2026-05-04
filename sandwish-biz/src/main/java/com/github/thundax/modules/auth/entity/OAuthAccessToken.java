package com.github.thundax.modules.auth.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.enums.OAuthAccessTokenStatus;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OAuth2 access token 事实。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAccessToken implements Auditable {
    private EntityId id;
    private String tokenId;
    private String tokenHash;
    private String clientId;
    private EntityId userId;
    private Set<String> scopes = new LinkedHashSet<>();
    private Date issuedAt;
    private Date expireAt;
    private OAuthAccessTokenStatus status = OAuthAccessTokenStatus.ACTIVE;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public boolean isActive() {
        return OAuthAccessTokenStatus.ACTIVE == status;
    }

    public boolean isExpired(Date now) {
        return expireAt != null && now != null && !expireAt.after(now);
    }

    public boolean isIntrospectionActive(Date now) {
        return isActive() && !isExpired(now);
    }

    public void revoke(Date updateTime) {
        this.status = OAuthAccessTokenStatus.REVOKED;
        this.updateDate = updateTime;
    }

    public void expire(Date updateTime) {
        this.status = OAuthAccessTokenStatus.EXPIRED;
        this.updateDate = updateTime;
    }
}
