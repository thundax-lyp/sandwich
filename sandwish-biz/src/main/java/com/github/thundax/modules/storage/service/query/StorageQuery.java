package com.github.thundax.modules.storage.service.query;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorageQuery {
    private StoredObjectId id;
    private List<StoredObjectId> ids;
    private String contentType;
    private String referenceOwnerId;
    private String referenceOwnerType;
    private String ownerId;
    private StorageOwnerType ownerType;
    private StoredObjectStatus objectStatus;
    private StoredObjectReferenceStatus referenceStatus;
    private String originalFilename;
    private String remarks;
    private SortDirection sortDirection = SortDirection.ASC;
}
