package com.github.thundax.modules.storage.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.storage.entity.enums.MultipartUploadStatus;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class MultipartUploadSession {

    private EntityId id;
    private String uploadId;
    private String ownerId;
    private StorageOwnerType ownerType;
    private String businessType;
    private String originalFilename;
    private String mimeType;
    private StorageType storageType = StorageType.LOCAL_FILE;
    private String bucketName;
    private String objectKey;
    private String providerUploadId;
    private Long totalSize;
    private Long partSize;
    private Integer uploadedPartCount = 0;
    private MultipartUploadStatus uploadStatus = MultipartUploadStatus.INITIATED;
    private Date completedDate;
    private Date abortedDate;

    public void setOwnerType(String ownerType) {
        this.ownerType = StringUtils.isBlank(ownerType) ? null : StorageOwnerType.from(ownerType);
    }

    public void setOwnerType(StorageOwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public void setStorageType(String storageType) {
        this.storageType = StringUtils.isBlank(storageType) ? null : StorageType.from(storageType);
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = StringUtils.isBlank(uploadStatus) ? null : MultipartUploadStatus.from(uploadStatus);
    }

    public void setUploadStatus(MultipartUploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }
}
