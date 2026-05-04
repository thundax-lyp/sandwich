package com.github.thundax.modules.storage.dao;

import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import java.util.List;

public interface StorageBusinessDao {

    List<String> listBusinessTypes();

    List<StorageBusiness> listBusiness(Storage entity);

    void insertBusiness(List<StorageBusiness> list);

    void deleteBusiness(String id);

    int deleteBusinessByBusiness(String businessType, String businessId);
}
