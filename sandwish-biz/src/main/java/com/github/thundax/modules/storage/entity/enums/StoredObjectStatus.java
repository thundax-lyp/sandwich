package com.github.thundax.modules.storage.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum StoredObjectStatus {
    ACTIVE,
    DELETING,
    DELETED;

    private static final String LEGACY_ACTIVE = "1";
    private static final String LEGACY_INACTIVE = "0";

    public String value() {
        return name();
    }

    public static StoredObjectStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value)
                        || item.legacyValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown storage status: " + value));
    }

    private String legacyValue() {
        return this == ACTIVE ? LEGACY_ACTIVE : LEGACY_INACTIVE;
    }
}
