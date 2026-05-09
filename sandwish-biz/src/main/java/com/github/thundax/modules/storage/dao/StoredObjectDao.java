package com.github.thundax.modules.storage.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import java.util.List;

public interface StoredObjectDao {

    StoredObject getById(StoredObjectId id);

    List<StoredObject> listByIds(List<Long> idList);

    List<StoredObject> list(
            String mimeType,
            String ownerId,
            String ownerType,
            String objectStatus,
            String referenceStatus,
            String referenceOwnerId,
            String referenceOwnerType,
            String name,
            String remarks);

    Page<StoredObject> page(
            String mimeType,
            String ownerId,
            String ownerType,
            String objectStatus,
            String referenceStatus,
            String referenceOwnerId,
            String referenceOwnerType,
            String name,
            String remarks,
            int pageNo,
            int pageSize);

    StoredObjectId insert(StoredObject entity);

    int update(StoredObject entity);

    int deleteById(StoredObjectId id);

    List<String> listMimeTypes();

    int updateObjectStatus(StoredObject storage);

    int updateReferenceStatus(StoredObject storage);
}
