package com.github.thundax.modules.storage.dao;

import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.StoredObjectReference;
import java.util.List;

public interface StoredObjectReferenceDao {

    List<String> listBusinessTypes();

    List<StoredObjectReference> listBusiness(StoredObject entity);

    void insertBusiness(List<StoredObjectReference> list);

    void deleteBusiness(String id);

    int deleteBusinessByBusiness(String businessType, String businessId);
}
