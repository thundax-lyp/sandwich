package com.github.thundax.modules.storage.entity.enums;

import com.github.thundax.common.exception.BizException;
import java.util.Arrays;

public enum MultipartUploadStatus {
    INITIATED,
    UPLOADING,
    COMPLETED,
    ABORTED;

    public String value() {
        return name();
    }

    public static MultipartUploadStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException("Unknown multipart upload status: " + value));
    }
}
