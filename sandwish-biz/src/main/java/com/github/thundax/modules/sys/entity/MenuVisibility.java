package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.config.Global;
import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum MenuVisibility {
    VISIBLE,
    HIDDEN;

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
        return this == VISIBLE ? Global.SHOW : Global.HIDE;
    }
}
