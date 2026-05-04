package com.github.thundax.modules.storage.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoredObjectReference {
    private EntityId objectId;

    private String ownerId;
    private StorageOwnerType ownerType;
    private String ownerParams;

    public EntityId getId() {
        return objectId;
    }

    public void setId(EntityId id) {
        this.objectId = id;
    }

    public String getBusinessId() {
        return ownerId;
    }

    public void setBusinessId(String businessId) {
        this.ownerId = businessId;
    }

    public String getBusinessType() {
        return ownerType == null ? null : ownerType.value();
    }

    public void setBusinessType(String businessType) {
        this.ownerType = businessType == null ? null : StorageOwnerType.from(businessType);
    }

    public String getBusinessParams() {
        return ownerParams;
    }

    public void setBusinessParams(String businessParams) {
        this.ownerParams = businessParams;
    }

    @Deprecated
    public StoredObjectReferenceStatus getVisibility() {
        return null;
    }

    @Deprecated
    public void setVisibility(StoredObjectReferenceStatus visibility) {}

    @Deprecated
    public boolean isPublic() {
        return false;
    }
}
