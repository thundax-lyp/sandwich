package com.github.thundax.modules.storage.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum StoredObjectReferenceStatus {
    UNREFERENCED,
    REFERENCED;

    private static final String LEGACY_REFERENCED = "1";
    private static final String LEGACY_UNREFERENCED = "0";

    public String value() {
        return name();
    }

    public static StoredObjectReferenceStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value)
                        || item.legacyValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown storage reference status: " + value));
    }

    private String legacyValue() {
        return this == REFERENCED ? LEGACY_REFERENCED : LEGACY_UNREFERENCED;
    }
}
