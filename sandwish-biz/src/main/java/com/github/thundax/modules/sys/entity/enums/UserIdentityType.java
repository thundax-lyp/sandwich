package com.github.thundax.modules.sys.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum UserIdentityType {
    ACCOUNT,
    MOBILE,
    EMAIL,
    WECOM,
    GITHUB;

    public String value() {
        return name();
    }

    public static UserIdentityType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown user identity type: " + value));
    }
}
