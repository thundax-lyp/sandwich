package com.github.thundax.modules.storage.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class MultipartUploadPartId extends BaseLongId {

    private MultipartUploadPartId(Long value) {
        super(value);
    }

    public static MultipartUploadPartId of(Long value) {
        return new MultipartUploadPartId(value);
    }

    public static MultipartUploadPartId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
