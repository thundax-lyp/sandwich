package com.github.thundax.modules.sys.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum MenuVisibility {
    VISIBLE,
    HIDDEN;

    private static final String LEGACY_VISIBLE = "1";
    private static final String LEGACY_HIDDEN = "0";

    public String value() {
        return name();
    }

    public static MenuVisibility from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value)
                        || item.legacyValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown menu visibility: " + value));
    }

    private String legacyValue() {
        return this == VISIBLE ? LEGACY_VISIBLE : LEGACY_HIDDEN;
    }
}
