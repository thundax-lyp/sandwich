package com.github.thundax.modules.storage.entity;

import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoredObjectReference {
    private StoredObjectId objectId;

    private String ownerId;
    private StorageOwnerType ownerType;
    private String ownerParams;
    private StoredObjectReferenceStatus referenceStatus;

    public StoredObjectId getId() {
        return objectId;
    }

    public void setId(StoredObjectId id) {
        this.objectId = id;
    }

    public String getBusinessId() {
        return ownerId;
    }

    public void setBusinessId(String referenceOwnerId) {
        this.ownerId = referenceOwnerId;
    }

    public String getBusinessType() {
        return ownerType == null ? null : ownerType.value();
    }

    public void setBusinessType(String referenceOwnerType) {
        this.ownerType = referenceOwnerType == null ? null : StorageOwnerType.from(referenceOwnerType);
    }

    public String getBusinessParams() {
        return ownerParams;
    }

    public void setBusinessParams(String businessParams) {
        this.ownerParams = businessParams;
    }
}
