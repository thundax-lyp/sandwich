package com.github.thundax.modules.auth.entity.enums;

import com.github.thundax.common.exception.DomainException;
import java.util.Arrays;

public enum OAuthClientStatus {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static OAuthClientStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AUTH-90001",
                        "auth.domain.oauth-client-status.invalid",
                        "Unknown oauth client status: " + value));
    }
}
