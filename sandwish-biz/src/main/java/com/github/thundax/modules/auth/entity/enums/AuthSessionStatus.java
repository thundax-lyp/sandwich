package com.github.thundax.modules.auth.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum AuthSessionStatus {
    ACTIVE,
    LOGGED_OUT,
    INVALIDATED,
    EXPIRED;

    public String value() {
        return name();
    }

    public static AuthSessionStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown auth session status: " + value));
    }
}
