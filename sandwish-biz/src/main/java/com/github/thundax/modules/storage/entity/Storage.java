package com.github.thundax.modules.storage.entity;

import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.entity.enums.StorageBackendType;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageStatus;
import com.github.thundax.modules.storage.entity.enums.StorageVisibility;
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
public class Storage implements Sortable {
    private EntityId id;
    private String name;
    private String extendName;
    private String mimeType;
    private String ownerId;
    private StorageOwnerType ownerType;
    private StorageBackendType storageType = StorageBackendType.LOCAL_FILE;
    private String bucketName;
    private String objectKey;
    private Long size;
    private String accessEndpoint;
    private StorageStatus status = StorageStatus.ENABLED;
    private StorageVisibility visibility = StorageVisibility.PRIVATE;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;

    public static final String BUSINESS_TYPE_UNDEFINED = "undefined";

    private static final String PATH_FORMAT = "yyyyMM";

    public void setStatus(String status) {
        this.status = StringUtils.isBlank(status) ? null : StorageStatus.from(status);
    }

    public void setStatus(StorageStatus status) {
        this.status = status;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = StringUtils.isBlank(ownerType) ? null : StorageOwnerType.from(ownerType);
    }

    public void setOwnerType(StorageOwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public void setStorageType(String storageType) {
        this.storageType = StringUtils.isBlank(storageType) ? null : StorageBackendType.from(storageType);
    }

    public void setStorageType(StorageBackendType storageType) {
        this.storageType = storageType;
    }

    public boolean isEnable() {
        return StorageStatus.ENABLED == getStatus();
    }

    public String getFileName() {
        return EntityIdCodec.toValue(getId()) + MetaFile.DOT + this.getExtendName();
    }

    public String getOriginalFileName() {
        return this.getName() + MetaFile.DOT + this.getExtendName();
    }

    public String getPathName() {
        return new SimpleDateFormat(PATH_FORMAT).format(this.getCreateDate()) + MetaFile.SEPARATOR + this.getFileName();
    }
}
