package com.github.thundax.modules.auth.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.id.EntityId;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OAuth2 授权请求和授权码事实。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAuthorization implements Auditable {
    private EntityId id;
    private String authorizationCode;
    private String clientId;
    private String tenantId;
    private EntityId userId;
    private String redirectUri;
    private Set<String> scopes = new LinkedHashSet<>();
    private String state;
    private String codeChallenge;
    private String codeChallengeMethod;
    private Date issuedAt;
    private Date expireAt;
    private boolean used;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public boolean isExpired(Date now) {
        return expireAt != null && now != null && !expireAt.after(now);
    }

    public boolean canConsume(Date now) {
        return !used && !isExpired(now);
    }

    public void markUsed(Date updateTime) {
        this.used = true;
        this.updateDate = updateTime;
    }
}
