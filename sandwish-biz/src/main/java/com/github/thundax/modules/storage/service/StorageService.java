package com.github.thundax.modules.storage.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.modules.storage.backend.StorageBackendObject;
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

    void add(StoredObject storage);

    void update(StoredObject storage);

    int deleteById(EntityId id);

    int batchDeleteById(List<EntityId> ids);

    List<String> listMimeTypes();

    List<String> listReferenceOwnerTypes();

    int updateObjectStatus(StoredObject storage);

    int updateReferenceStatus(StoredObject storage);

    int removeReferences(StorageOwnerType ownerType, String ownerId);

    void addReferences(List<StoredObjectReference> list);

    List<StoredObjectReference> listReferences(StoredObject entity);

    boolean canReadContent(StoredObject storage, StorageOwnerType ownerType, String ownerId);

    MultipartUploadSession initMultipartUpload(MultipartUploadSession session);

    MultipartUploadPart uploadMultipartPart(MultipartUploadPart part);

    StoredObject completeMultipartUpload(String uploadId, StorageBackendObject object);

    int abortMultipartUpload(String uploadId);
}
