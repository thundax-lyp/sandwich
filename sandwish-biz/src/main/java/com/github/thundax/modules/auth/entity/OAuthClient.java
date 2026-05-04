package com.github.thundax.modules.auth.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.enums.OAuthClientStatus;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OAuth2 客户端配置。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClient implements Auditable {
    private EntityId id;
    private String clientId;
    private String clientSecretHash;
    private String clientName;
    private String clientType;
    private Set<String> grantTypes = new LinkedHashSet<>();
    private Set<String> scopes = new LinkedHashSet<>();
    private Set<String> redirectUris = new LinkedHashSet<>();
    private long accessTokenTtlSeconds;
    private long refreshTokenTtlSeconds;
    private OAuthClientStatus status = OAuthClientStatus.ENABLED;
    private String contact;
    private String remark;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public boolean isEnabled() {
        return OAuthClientStatus.ENABLED == status;
    }

    public boolean supportsGrantType(String grantType) {
        return grantType != null && grantTypes != null && grantTypes.contains(grantType);
    }

    public boolean supportsRedirectUri(String redirectUri) {
        return redirectUri != null && redirectUris != null && redirectUris.contains(redirectUri);
    }

    public boolean supportsScopes(Set<String> requestedScopes) {
        return requestedScopes != null && scopes != null && scopes.containsAll(requestedScopes);
    }
}
