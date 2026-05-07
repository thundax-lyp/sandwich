package com.github.thundax.modules.member.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum MemberAuthSessionStatus {
    ACTIVE,
    LOGGED_OUT,
    INVALIDATED,
    EXPIRED;

    public String value() {
        return name();
    }

    public static MemberAuthSessionStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown member auth session status: " + value));
    }
}
