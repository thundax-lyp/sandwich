package com.github.thundax.modules.storage.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.StoredObjectReference;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import java.util.List;

public interface StorageService {

    StoredObject getById(EntityId id);

    List<StoredObject> listByIds(List<EntityId> ids);

    List<StoredObject> list(StorageQuery query);

    PageDTO<StoredObject> page(StorageQuery query, PageDTO<StoredObject> page);

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

    @LayerPublicApi(reason = "分片上传流程初始化会话的业务入口")
    MultipartUploadSession initMultipartUpload(MultipartUploadSession session);

    @LayerPublicApi(reason = "分片上传流程写入单个分片的业务入口")
    MultipartUploadPart uploadMultipartPart(MultipartUploadPart part);

    @LayerPublicApi(reason = "分片上传流程合并并生成存储对象的业务入口")
    StoredObject completeMultipartUpload(String uploadId, StoredObject object);

    @LayerPublicApi(reason = "分片上传流程取消会话的业务入口")
    int abortMultipartUpload(String uploadId);
}
