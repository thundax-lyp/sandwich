package com.github.thundax.modules.storage.backend;

import com.github.thundax.modules.storage.entity.enums.StorageBackendType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageBackendObject {

    private StorageBackendType storageType;
    private String bucketName;
    private String objectKey;
    private Long size;
    private String accessEndpoint;
}
