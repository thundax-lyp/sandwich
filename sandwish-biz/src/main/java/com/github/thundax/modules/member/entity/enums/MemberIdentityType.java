package com.github.thundax.modules.member.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum MemberIdentityType {
    ACCOUNT,
    MOBILE,
    EMAIL;

    public String value() {
        return name();
    }

    public static MemberIdentityType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown member identity type: " + value));
    }
}
