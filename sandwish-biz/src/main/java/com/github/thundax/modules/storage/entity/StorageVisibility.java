package com.github.thundax.modules.storage.entity;

import com.github.thundax.common.config.Global;
import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum StorageVisibility {
    PUBLIC,
    PRIVATE;

    public String value() {
        return name();
    }

    public static StorageVisibility from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value)
                        || item.legacyValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown storage visibility: " + value));
    }

    private String legacyValue() {
        return this == PUBLIC ? Global.YES : Global.NO;
    }
}
