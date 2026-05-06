package com.github.thundax.modules.storage.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum StoredObjectReferenceStatus {
    UNREFERENCED,
    REFERENCED;

    public String value() {
        return name();
    }

    public static StoredObjectReferenceStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown storage reference status: " + value));
    }
}
