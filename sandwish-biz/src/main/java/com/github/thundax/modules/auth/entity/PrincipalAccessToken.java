package com.github.thundax.modules.auth.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.enums.PrincipalTokenStatus;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalAccessToken {
    private EntityId id;
    private String tokenId;
    private String tokenHash;
    private String clientId;
    private String sessionId;
    private PrincipalKey principalKey;
    private Set<String> scopes = new LinkedHashSet<>();
    private Date issuedAt;
    private Date expireAt;
    private PrincipalTokenStatus status = PrincipalTokenStatus.ACTIVE;

    public boolean canAccess(Date now) {
        return isActive() && !isExpired(now);
    }

    public void revoke(Date now) {
        status = PrincipalTokenStatus.REVOKED;
    }

    public void expire(Date now) {
        status = PrincipalTokenStatus.EXPIRED;
    }

    public boolean isActive() {
        return status == PrincipalTokenStatus.ACTIVE;
    }

    public boolean isExpired(Date now) {
        return expireAt != null && now != null && !expireAt.after(now);
    }
}
