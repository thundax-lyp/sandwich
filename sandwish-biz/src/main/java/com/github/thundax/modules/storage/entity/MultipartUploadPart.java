package com.github.thundax.modules.storage.entity;

import com.github.thundax.common.id.EntityId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MultipartUploadPart {

    private EntityId id;
    private String uploadId;
    private Integer partNumber;
    private String etag;
    private Long size;
}
