package com.github.thundax.modules.storage.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.modules.storage.backend.StorageBackendObject;
import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import java.util.List;

public interface StorageService {

    Storage getById(EntityId id);

    List<Storage> listByIds(List<EntityId> ids);

    List<Storage> list(StorageQuery query);

    PageDTO<Storage> page(StorageQuery query, PageDTO<Storage> page);

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

    boolean canAccess(Storage storage, StorageOwnerType ownerType, String ownerId);

    MultipartUploadSession initMultipartUpload(MultipartUploadSession session);

    MultipartUploadPart uploadMultipartPart(MultipartUploadPart part);

    Storage completeMultipartUpload(String uploadId, StorageBackendObject object);

    int abortMultipartUpload(String uploadId);
}
