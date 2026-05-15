package com.github.thundax.modules.storage.entity;

import com.github.thundax.modules.storage.entity.enums.MultipartUploadStatus;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.valueobject.MultipartUploadSessionId;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class MultipartUploadSession {

    private MultipartUploadSessionId id;
    private String uploadId;
    private String ownerId;
    private StorageOwnerType ownerType;
    private String businessType;
    private String originalFilename;
    private String mimeType;
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

    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = StringUtils.isBlank(uploadStatus) ? null : MultipartUploadStatus.from(uploadStatus);
    }

    public void setUploadStatus(MultipartUploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }
}
