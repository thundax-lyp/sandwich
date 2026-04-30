package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum UserPrivilege {
    NORMAL,
    ADMIN,
    SUPER;

    public String value() {
        return name();
    }

    public static UserPrivilege from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown user privilege: " + value));
    }
}
