package com.github.thundax.modules.storage.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.storage.dao.StoredObjectDao;
import com.github.thundax.modules.storage.dao.StoredObjectReferenceDao;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.StoredObjectReference;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.service.command.AddStorageReferencesCommand;
import com.github.thundax.modules.storage.service.command.ChangeStorageCommand;
import com.github.thundax.modules.storage.service.command.ChangeStorageObjectStatusCommand;
import com.github.thundax.modules.storage.service.command.ChangeStorageReferenceStatusCommand;
import com.github.thundax.modules.storage.service.command.CreateStorageCommand;
import com.github.thundax.modules.storage.service.command.DeleteStorageCommand;
import com.github.thundax.modules.storage.service.command.RemoveStorageReferencesCommand;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import java.util.List;
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
    public StoredObject get(StoredObjectId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<StoredObject> list(StorageQuery query) {
        if (query != null && query.getIds() != null) {
            return dao.listByIds(StoredObjectIdCodec.toValues(query.getIds()));
        }
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
    public PageResult<StoredObject> page(StorageQuery query, PageQuery page) {
        PageQuery normalizedPage = normalizePage(page);
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
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoredObjectId create(CreateStorageCommand command) {
        StoredObject storage = toStoredObject(command);
        storage.setId(dao.insert(storage));
        return storage.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void change(ChangeStorageCommand command) {
        dao.update(toStoredObject(command));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int remove(DeleteStorageCommand command) {
        if (command == null || command.getId() == null) {
            return 0;
        }
        return dao.deleteById(command.getId());
    }

    @Override
    public List<String> listMimeTypes(StorageQuery query) {
        return dao.listMimeTypes();
    }

    @Override
    public List<String> listReferenceOwnerTypes(StorageQuery query) {
        return businessDao.listReferenceOwnerTypes();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeObjectStatus(ChangeStorageObjectStatusCommand command) {
        StoredObject storage = new StoredObject();
        storage.setId(command.getId());
        storage.setObjectStatus(command.getObjectStatus());
        return dao.updateObjectStatus(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeReferenceStatus(ChangeStorageReferenceStatusCommand command) {
        StoredObject storage = new StoredObject();
        storage.setId(command.getId());
        storage.setReferenceStatus(command.getReferenceStatus());
        return dao.updateReferenceStatus(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeReferences(RemoveStorageReferencesCommand command) {
        if (command == null) {
            return 0;
        }
        return businessDao.deleteByOwner(ownerTypeValue(command.getOwnerType()), command.getOwnerId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addReferences(AddStorageReferencesCommand command) {
        businessDao.insertReferences(command.getReferences());
    }

    @Override
    public List<StoredObjectReference> listReferences(StorageQuery query) {
        StoredObject entity = new StoredObject();
        entity.setId(query.getId());
        return businessDao.listReferences(entity);
    }

    @Override
    public boolean existsReadableContent(StorageQuery query) {
        StoredObject storage = query == null ? null : get(query.getId());
        if (storage == null) {
            return false;
        }
        if (StoredObjectReferenceStatus.REFERENCED == storage.getReferenceStatus()) {
            return true;
        }
        return StoredObjectReferenceStatus.UNREFERENCED == storage.getReferenceStatus()
                && storage.getOwnerType() == query.getOwnerType()
                && StringUtils.isNotBlank(query.getOwnerId())
                && StringUtils.equals(storage.getOwnerId(), query.getOwnerId());
    }

    private PageQuery normalizePage(PageQuery page) {
        PageQuery normalizedPage = page == null ? new PageQuery() : page;
        normalizedPage.normalize();
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

    private StoredObject toStoredObject(CreateStorageCommand command) {
        StoredObject storage = new StoredObject();
        storage.setId(command.getId());
        storage.setOriginalFilename(command.getOriginalFilename());
        storage.setContentType(command.getContentType());
        storage.setName(command.getName());
        storage.setExtendName(command.getExtendName());
        storage.setMimeType(command.getMimeType());
        storage.setOwnerId(command.getOwnerId());
        storage.setOwnerType(command.getOwnerType());
        storage.setStorageType(command.getStorageType());
        storage.setBucketName(command.getBucketName());
        storage.setObjectKey(command.getObjectKey());
        storage.setSize(command.getSize());
        storage.setAccessEndpoint(command.getAccessEndpoint());
        storage.setObjectStatus(command.getObjectStatus());
        storage.setReferenceStatus(command.getReferenceStatus());
        storage.setPriority(command.getPriority());
        storage.setRemarks(command.getRemarks());
        return storage;
    }

    private StoredObject toStoredObject(ChangeStorageCommand command) {
        StoredObject storage = new StoredObject();
        storage.setId(command.getId());
        storage.setOriginalFilename(command.getOriginalFilename());
        storage.setContentType(command.getContentType());
        storage.setName(command.getName());
        storage.setExtendName(command.getExtendName());
        storage.setMimeType(command.getMimeType());
        storage.setOwnerId(command.getOwnerId());
        storage.setOwnerType(command.getOwnerType());
        storage.setStorageType(command.getStorageType());
        storage.setBucketName(command.getBucketName());
        storage.setObjectKey(command.getObjectKey());
        storage.setSize(command.getSize());
        storage.setAccessEndpoint(command.getAccessEndpoint());
        storage.setObjectStatus(command.getObjectStatus());
        storage.setReferenceStatus(command.getReferenceStatus());
        storage.setPriority(command.getPriority());
        storage.setRemarks(command.getRemarks());
        return storage;
    }
}
