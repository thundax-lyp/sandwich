package com.github.thundax.modules.storage.dao;

import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.StoredObjectReference;
import java.util.List;

public interface StoredObjectReferenceDao {

    List<String> listReferenceOwnerTypes();

    List<StoredObjectReference> listReferences(StoredObject entity);

    void insertReferences(List<StoredObjectReference> list);

    void deleteByObjectId(String id);

    int deleteByOwner(String referenceOwnerType, String referenceOwnerId);
}
