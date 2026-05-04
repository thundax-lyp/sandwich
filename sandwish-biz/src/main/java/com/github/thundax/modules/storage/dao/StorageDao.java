package com.github.thundax.modules.storage.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.storage.entity.Storage;
import java.util.List;

public interface StorageDao {

    Storage getById(EntityId id);

    List<Storage> listByIds(List<String> idList);

    List<Storage> list(
            String mimeType,
            String ownerId,
            String ownerType,
            String enableFlag,
            String publicFlag,
            String businessId,
            String businessType,
            String name,
            String remarks);

    Page<Storage> page(
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

    String insert(Storage entity);

    int update(Storage entity);

    int deleteById(EntityId id);

    List<String> listMimeTypes();

    int updateStatus(Storage storage);

    int updateVisibility(Storage storage);
}
