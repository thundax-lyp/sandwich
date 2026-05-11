package com.github.thundax.modules.sys.entity.enums;

import com.github.thundax.common.exception.DomainException;
import java.util.Arrays;

public enum MenuVisibility {
    VISIBLE,
    HIDDEN;

    public String value() {
        return name();
    }

    public static MenuVisibility from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "SYS-90002", "sys.domain.menu-visibility.invalid", "Unknown menu visibility: " + value));
    }
}
