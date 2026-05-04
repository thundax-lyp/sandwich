package com.github.thundax.modules.storage.service.query;

import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageQuery implements Serializable {
    private String mimeType;
    private String businessId;
    private String businessType;
    private String ownerId;
    private StorageOwnerType ownerType;
    private StoredObjectStatus status;
    private StoredObjectReferenceStatus visibility;
    private String name;
    private String remarks;
}
