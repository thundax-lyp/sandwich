package com.github.thundax.modules.member.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum MemberIdentityStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static MemberIdentityStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown member identity status: " + value));
    }
}
