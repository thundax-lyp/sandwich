package com.github.thundax.modules.audit.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

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
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown audit action: " + value));
    }
}
