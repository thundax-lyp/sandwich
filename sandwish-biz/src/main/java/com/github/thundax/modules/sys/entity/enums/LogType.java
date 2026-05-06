package com.github.thundax.modules.sys.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum LogType {
    ACCESS,
    EXCEPTION;

    public String value() {
        return name();
    }

    public static LogType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown log type: " + value));
    }
}
