package com.github.thundax.modules.sys.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum RoleStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static RoleStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown role status: " + value));
    }
}
