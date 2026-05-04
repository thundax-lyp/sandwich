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
    private String referenceOwnerId;
    private String referenceOwnerType;
    private String ownerId;
    private StorageOwnerType ownerType;
    private StoredObjectStatus objectStatus;
    private StoredObjectReferenceStatus referenceStatus;
    private String name;
    private String remarks;
}
