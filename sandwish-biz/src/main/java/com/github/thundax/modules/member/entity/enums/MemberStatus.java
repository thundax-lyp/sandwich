package com.github.thundax.modules.member.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum MemberStatus {
    ENABLED,
    DISABLED;

    private static final String LEGACY_ENABLED = "1";
    private static final String LEGACY_DISABLED = "0";

    public String value() {
        return name();
    }

    public static MemberStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value)
                        || item.legacyValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown member status: " + value));
    }

    private String legacyValue() {
        return this == ENABLED ? LEGACY_ENABLED : LEGACY_DISABLED;
    }
}
