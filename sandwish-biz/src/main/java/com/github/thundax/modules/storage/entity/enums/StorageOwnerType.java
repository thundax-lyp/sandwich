package com.github.thundax.modules.storage.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum StorageOwnerType {
    USER,
    MEMBER;

    public String value() {
        return name();
    }

    public static StorageOwnerType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown storage owner type: " + value));
    }
}
