package com.github.thundax.modules.storage.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import java.util.List;

public interface StorageService {

    Storage getById(EntityId id);

    List<Storage> batchGetByIds(List<EntityId> ids);

    List<Storage> list(StorageQuery query);

    Page<Storage> page(StorageQuery query, Page<Storage> page);

    void add(Storage storage);

    void update(Storage storage);

    int deleteById(EntityId id);

    int batchDeleteById(List<EntityId> ids);

    List<String> listMimeTypes();

    List<String> listBusinessTypes();

    int updateStatus(Storage storage);

    int updateVisibility(Storage storage);

    int removeBusiness(String businessType, String businessId);

    void insertBusiness(List<StorageBusiness> list);

    List<StorageBusiness> listBusiness(Storage entity);
}
