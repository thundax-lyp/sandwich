package com.github.thundax.modules.auth.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum PrincipalIdentityStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static PrincipalIdentityStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown principal identity status: " + value));
    }
}
