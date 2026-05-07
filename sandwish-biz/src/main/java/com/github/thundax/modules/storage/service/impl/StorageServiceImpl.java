package com.github.thundax.modules.storage.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.modules.storage.dao.StoredObjectDao;
import com.github.thundax.modules.storage.dao.StoredObjectReferenceDao;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.StoredObjectReference;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StorageServiceImpl implements StorageService {

    private final StoredObjectDao dao;
    private final StoredObjectReferenceDao businessDao;

    public StorageServiceImpl(StoredObjectDao dao, StoredObjectReferenceDao businessDao) {
        this.dao = dao;
        this.businessDao = businessDao;
    }

    @Override
    public StoredObject getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<StoredObject> listByIds(List<EntityId> ids) {
        return dao.listByIds(EntityIdCodec.toValues(ids));
    }

    @Override
    public List<StoredObject> list(StorageQuery query) {
        return dao.list(
                query == null ? null : query.getContentType(),
                query == null ? null : query.getOwnerId(),
                query == null ? null : ownerTypeValue(query.getOwnerType()),
                query == null ? null : statusValue(query.getObjectStatus()),
                query == null ? null : referenceStatusValue(query.getReferenceStatus()),
                query == null ? null : query.getReferenceOwnerId(),
                query == null ? null : query.getReferenceOwnerType(),
                query == null ? null : query.getOriginalFilename(),
                query == null ? null : query.getRemarks());
    }

    @Override
    public PageDTO<StoredObject> page(StorageQuery query, PageDTO<StoredObject> page) {
        PageDTO<StoredObject> normalizedPage = normalizePage(page);
        IPage<StoredObject> dataPage = dao.page(
                query == null ? null : query.getContentType(),
                query == null ? null : query.getOwnerId(),
                query == null ? null : ownerTypeValue(query.getOwnerType()),
                query == null ? null : statusValue(query.getObjectStatus()),
                query == null ? null : referenceStatusValue(query.getReferenceStatus()),
                query == null ? null : query.getReferenceOwnerId(),
                query == null ? null : query.getReferenceOwnerType(),
                query == null ? null : query.getOriginalFilename(),
                query == null ? null : query.getRemarks(),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId add(StoredObject storage) {
        storage.setId(dao.insert(storage));
        return storage.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(StoredObject storage) {
        dao.update(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        return id == null ? 0 : dao.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<EntityId> ids) {
        return batchOperate(ids, this::deleteById);
    }

    @Override
    public List<String> listMimeTypes() {
        return dao.listMimeTypes();
    }

    @Override
    public List<String> listReferenceOwnerTypes() {
        return businessDao.listReferenceOwnerTypes();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateObjectStatus(StoredObject storage) {
        return dao.updateObjectStatus(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateReferenceStatus(StoredObject storage) {
        return dao.updateReferenceStatus(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeReferences(StorageOwnerType ownerType, String ownerId) {
        return businessDao.deleteByOwner(ownerTypeValue(ownerType), ownerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addReferences(List<StoredObjectReference> list) {
        businessDao.insertReferences(list);
    }

    @Override
    public List<StoredObjectReference> listReferences(StoredObject entity) {
        return businessDao.listReferences(entity);
    }

    @Override
    public boolean canReadContent(StoredObject storage, StorageOwnerType ownerType, String ownerId) {
        if (storage == null) {
            return false;
        }
        if (StoredObjectReferenceStatus.REFERENCED == storage.getReferenceStatus()) {
            return true;
        }
        return StoredObjectReferenceStatus.UNREFERENCED == storage.getReferenceStatus()
                && storage.getOwnerType() == ownerType
                && StringUtils.isNotBlank(ownerId)
                && StringUtils.equals(storage.getOwnerId(), ownerId);
    }

    private <T> int batchOperate(Collection<T> collection, Function<T, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (T entity : collection) {
                count += operator.apply(entity);
            }
        }
        return count;
    }

    private PageDTO<StoredObject> normalizePage(PageDTO<StoredObject> page) {
        PageDTO<StoredObject> normalizedPage = page == null ? new PageDTO<>() : page;
        normalizedPage.initialize();
        return normalizedPage;
    }

    private String ownerTypeValue(StorageOwnerType ownerType) {
        return ownerType == null ? null : ownerType.value();
    }

    private String statusValue(StoredObjectStatus status) {
        return status == null ? null : status.value();
    }

    private String referenceStatusValue(StoredObjectReferenceStatus referenceStatus) {
        return referenceStatus == null ? null : referenceStatus.value();
    }
}
