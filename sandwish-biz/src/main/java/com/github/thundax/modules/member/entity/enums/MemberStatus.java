package com.github.thundax.modules.member.entity.enums;

import com.github.thundax.common.exception.DomainException;
import java.util.Arrays;

public enum MemberStatus {
    PENDING,
    ACTIVE,
    SUSPENDED,
    CLOSED;

    public String value() {
        return name();
    }

    public static MemberStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "MEMBER-90002", "member.domain.status.invalid", "Unknown member status: " + value));
    }
}
