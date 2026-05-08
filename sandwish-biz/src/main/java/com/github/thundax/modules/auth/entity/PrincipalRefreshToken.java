package com.github.thundax.modules.auth.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalTokenStatus;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalRefreshToken {
    private EntityId id;
    private String tokenId;
    private String tokenHash;
    private String accessTokenId;
    private String clientId;
    private String sessionId;
    private PrincipalKey principalKey;
    private Date issuedAt;
    private Date expireAt;
    private PrincipalTokenStatus status = PrincipalTokenStatus.ACTIVE;

    public boolean canRefresh(Date now) {
        return isActive() && !isExpired(now);
    }

    public void markUsed(Date now) {
        status = PrincipalTokenStatus.USED;
    }

    public void revoke(Date now) {
        status = PrincipalTokenStatus.REVOKED;
    }

    public void expire(Date now) {
        status = PrincipalTokenStatus.EXPIRED;
    }

    public boolean isActive() {
        return status != null && status.isActive();
    }

    public boolean isExpired(Date now) {
        return expireAt != null && now != null && !expireAt.after(now);
    }
}
