package com.github.thundax.modules.audit.entity.enums;

import com.github.thundax.common.exception.DomainException;
import java.util.Arrays;

public enum AuditOperatorType {
    USER,
    MEMBER,
    OPEN_CLIENT,
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
                .orElseThrow(() -> new DomainException(
                        "AUDIT-90002", "audit.domain.operator-type.invalid", "Unknown audit operator type: " + value));
    }
}
