package com.github.thundax.modules.assist.entity;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum AsyncTaskStatus {
    IDLE,
    ACTIVE,
    SUSPENDED,
    SUCCESS,
    ERROR;

    public String value() {
        return name();
    }

    public static AsyncTaskStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown async task status: " + value));
    }
}
