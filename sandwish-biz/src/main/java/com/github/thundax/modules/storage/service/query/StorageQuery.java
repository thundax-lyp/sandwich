package com.github.thundax.modules.storage.service.query;

import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageStatus;
import com.github.thundax.modules.storage.entity.enums.StorageVisibility;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class StorageQuery implements Serializable {
    private String mimeType;
    private String businessId;
    private String businessType;
    private String ownerId;
    private StorageOwnerType ownerType;
    private StorageStatus status;
    private StorageVisibility visibility;
    private String name;
    private String remarks;

    public void setOwnerType(String ownerType) {
        this.ownerType = StringUtils.isBlank(ownerType) ? null : StorageOwnerType.from(ownerType);
    }

    public void setOwnerType(StorageOwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public void setStatus(String status) {
        this.status = StringUtils.isBlank(status) ? null : StorageStatus.from(status);
    }

    public void setStatus(StorageStatus status) {
        this.status = status;
    }
}
