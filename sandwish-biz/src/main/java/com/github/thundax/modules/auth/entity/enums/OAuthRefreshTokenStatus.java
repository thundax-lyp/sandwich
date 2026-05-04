package com.github.thundax.modules.auth.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum OAuthRefreshTokenStatus {
    ACTIVE,
    USED,
    REVOKED,
    EXPIRED;

    public String value() {
        return name();
    }

    public static OAuthRefreshTokenStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown oauth refresh token status: " + value));
    }
}
