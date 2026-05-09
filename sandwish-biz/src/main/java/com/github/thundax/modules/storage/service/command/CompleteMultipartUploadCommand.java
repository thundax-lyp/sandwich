package com.github.thundax.modules.storage.service.command;

import com.github.thundax.modules.storage.entity.enums.StorageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompleteMultipartUploadCommand {
    private String uploadId;
    private StorageType storageType;
    private String bucketName;
    private String objectKey;
    private Long size;
    private String accessEndpoint;
}
