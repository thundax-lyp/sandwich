package com.github.thundax.modules.open.entity.enums;

import com.github.thundax.common.exception.DomainException;
import java.util.Arrays;

public enum OpenClientStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static OpenClientStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "OPEN-90001", "open.domain.client-status.invalid", "Unknown open client status: " + value));
    }
}
