package com.github.thundax.modules.auth.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum MemberRefreshTokenStatus {
    ACTIVE,
    USED,
    REVOKED,
    EXPIRED;

    public String value() {
        return name();
    }

    public static MemberRefreshTokenStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown member refresh token status: " + value));
    }
}
