package com.github.thundax.modules.audit.entity.enums;

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
        return AuditOperatorType.valueOf(value);
    }
}
