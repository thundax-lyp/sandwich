package com.github.thundax.modules.member.entity.enums;

import com.github.thundax.common.exception.DomainException;
import java.util.Arrays;

public enum MemberGender {
    MALE,
    FEMALE,
    PRIVATE;

    public String value() {
        return name();
    }

    public static MemberGender from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "MEMBER-90001", "member.domain.gender.invalid", "Unknown member gender: " + value));
    }
}
