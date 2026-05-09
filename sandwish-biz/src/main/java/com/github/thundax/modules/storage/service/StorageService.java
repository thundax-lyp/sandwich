package com.github.thundax.modules.storage.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.StoredObjectReference;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import java.util.List;

public interface StorageService {

    StoredObject getById(EntityId id);

    List<StoredObject> listByIds(List<EntityId> ids);

    List<StoredObject> list(StorageQuery query);

    PageResult<StoredObject> page(StorageQuery query, PageQuery page);

    EntityId add(StoredObject storage);

    void update(StoredObject storage);

    int deleteById(EntityId id);

    int batchDeleteById(List<EntityId> ids);

    List<String> listMimeTypes();

    List<String> listReferenceOwnerTypes();

    int updateObjectStatus(StoredObject storage);

    int updateReferenceStatus(StoredObject storage);

    @LayerPublicApi(reason = "业务对象删除或解绑时清理存储引用关系的跨模块入口")
    int removeReferences(StorageOwnerType ownerType, String ownerId);

    @LayerPublicApi(reason = "业务对象保存文件后写入存储引用关系的跨模块入口")
    void addReferences(List<StoredObjectReference> list);

    List<StoredObjectReference> listReferences(StoredObject entity);

    boolean canReadContent(StoredObject storage, StorageOwnerType ownerType, String ownerId);
}
