package com.github.thundax.modules.audit.entity.enums;

public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    ENABLE,
    DISABLE,
    ARCHIVE,
    RESTORE,
    BIND,
    UNBIND,
    UPDATE_RELATION,
    RESET_CREDENTIAL;

    public String value() {
        return name();
    }

    public static AuditAction from(String value) {
        if (value == null) {
            return null;
        }
        return AuditAction.valueOf(value);
    }
}
