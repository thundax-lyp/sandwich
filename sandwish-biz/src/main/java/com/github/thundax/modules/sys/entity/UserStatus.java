package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.config.Global;
import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum UserStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static UserStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value)
                        || item.legacyValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown user status: " + value));
    }

    private String legacyValue() {
        return this == ENABLED ? Global.ENABLE : Global.DISABLE;
    }
}
