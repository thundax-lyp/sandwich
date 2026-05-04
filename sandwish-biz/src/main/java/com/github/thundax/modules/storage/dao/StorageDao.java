package com.github.thundax.modules.storage.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.storage.entity.StoredObject;
import java.util.List;

public interface StorageDao {

    StoredObject getById(EntityId id);

    List<StoredObject> listByIds(List<String> idList);

    List<StoredObject> list(
            String mimeType,
            String ownerId,
            String ownerType,
            String enableFlag,
            String publicFlag,
            String businessId,
            String businessType,
            String name,
            String remarks);

    Page<StoredObject> page(
            String mimeType,
            String ownerId,
            String ownerType,
            String enableFlag,
            String publicFlag,
            String businessId,
            String businessType,
            String name,
            String remarks,
            int pageNo,
            int pageSize);

    String insert(StoredObject entity);

    int update(StoredObject entity);

    int deleteById(EntityId id);

    List<String> listMimeTypes();

    int updateStatus(StoredObject storage);

    int updateVisibility(StoredObject storage);
}
