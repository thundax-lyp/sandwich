package com.github.thundax.modules.auth.entity.enums;

import com.github.thundax.common.exception.DomainException;
import java.util.Arrays;

public enum PrincipalType {
    USER,
    MEMBER;

    public String value() {
        return name();
    }

    public static PrincipalType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AUTH-90011", "auth.domain.principal-type.invalid", "Unknown principal type: " + value));
    }
}
