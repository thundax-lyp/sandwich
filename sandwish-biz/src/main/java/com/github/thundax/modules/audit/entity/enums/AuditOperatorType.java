package com.github.thundax.modules.audit.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum AuditOperatorType {
    USER,
    MEMBER,
    SYSTEM,
    UNKNOWN;

    public String value() {
        return name();
    }

    public static AuditOperatorType from(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown audit operator type: " + value));
    }
}
