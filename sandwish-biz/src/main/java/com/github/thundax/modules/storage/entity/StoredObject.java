package com.github.thundax.modules.storage.entity;

import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import com.github.thundax.modules.storage.utils.MetaFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoredObject implements Sortable {
    private EntityId id;
    private String originalFilename;
    private String contentType;
    private String name;
    private String extendName;
    private String mimeType;
    private String ownerId;
    private StorageOwnerType ownerType;
    private StorageType storageType = StorageType.LOCAL_FILE;
    private String bucketName;
    private String objectKey;
    private Long size;
    private String accessEndpoint;
    private StoredObjectStatus objectStatus = StoredObjectStatus.ACTIVE;
    private StoredObjectReferenceStatus referenceStatus = StoredObjectReferenceStatus.UNREFERENCED;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;

    public static final String BUSINESS_TYPE_UNDEFINED = "undefined";

    private static final String PATH_FORMAT = "yyyyMM";

    public String getOriginalFilename() {
        return StringUtils.isBlank(originalFilename) ? getOriginalFileName() : originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return StringUtils.isBlank(contentType) ? mimeType : contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        this.mimeType = contentType;
    }

    public void setStatus(String status) {
        this.objectStatus = StringUtils.isBlank(status) ? null : StoredObjectStatus.from(status);
    }

    public void setStatus(StoredObjectStatus status) {
        this.objectStatus = status;
    }

    public StoredObjectStatus getStatus() {
        return objectStatus;
    }

    public void setObjectStatus(String objectStatus) {
        this.objectStatus = StringUtils.isBlank(objectStatus) ? null : StoredObjectStatus.from(objectStatus);
    }

    public void setObjectStatus(StoredObjectStatus objectStatus) {
        this.objectStatus = objectStatus;
    }

    public void setReferenceStatus(String referenceStatus) {
        this.referenceStatus =
                StringUtils.isBlank(referenceStatus) ? null : StoredObjectReferenceStatus.from(referenceStatus);
    }

    public void setReferenceStatus(StoredObjectReferenceStatus referenceStatus) {
        this.referenceStatus = referenceStatus;
    }

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

    public boolean isEnable() {
        return StoredObjectStatus.ACTIVE == getObjectStatus();
    }

    public String getFileName() {
        return EntityIdCodec.toValue(getId()) + MetaFile.DOT + this.getExtendName();
    }

    public String getOriginalFileName() {
        if (StringUtils.isNotBlank(originalFilename)) {
            return originalFilename;
        }
        if (StringUtils.isBlank(this.getExtendName())) {
            return this.getName();
        }
        return this.getName() + MetaFile.DOT + this.getExtendName();
    }

    public String getPathName() {
        return new SimpleDateFormat(PATH_FORMAT).format(this.getCreateDate()) + MetaFile.SEPARATOR + this.getFileName();
    }
}
