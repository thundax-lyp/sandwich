package com.github.thundax.modules.auth.entity;

import com.github.thundax.modules.auth.entity.valueobject.PrincipalAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalAuthSession {
    public static final String VALUE_PERMISSIONS = "PERMISSIONS";

    private PrincipalAuthSessionId id;
    private PrincipalKey principalKey;
    private String clientId;
    private Map<String, PrincipalAuthSessionValue> values = new LinkedHashMap<>();
    private Date issuedAt;
    private Date lastAccessTime;
    private Date expireAt;

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

    public Map<String, PrincipalAuthSessionValue> getValues() {
        if (values == null) {
            values = new LinkedHashMap<>();
        }
        return values;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrincipalAuthSessionValue {
        private Object value;
        private Date expiredAt;
    }
}
