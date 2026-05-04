package com.github.thundax.modules.auth.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum UserCredentialStatus {
    ACTIVE,
    LOCKED,
    EXPIRED,
    DISABLED;

    public String value() {
        return name();
    }

    public static UserCredentialStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown user credential status: " + value));
    }
}
