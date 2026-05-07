package com.github.thundax.modules.auth.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.id.EntityId;
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
public class PrincipalAccessToken implements Auditable {
    private EntityId id;
    private String tokenId;
    private String tokenHash;
    private String sessionId;
    private PrincipalKey principalKey;
    private Date issuedAt;
    private Date expireAt;
    private String status;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public boolean isExpired(Date now) {
        return expireAt != null && now != null && !expireAt.after(now);
    }
}
