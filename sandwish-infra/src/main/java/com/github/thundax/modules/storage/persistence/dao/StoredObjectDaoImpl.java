package com.github.thundax.modules.storage.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.dao.StoredObjectDao;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.persistence.assembler.StoragePersistenceAssembler;
import com.github.thundax.modules.storage.persistence.cache.StorageCacheSupport;
import com.github.thundax.modules.storage.persistence.dataobject.StorageDO;
import com.github.thundax.modules.storage.persistence.dataobject.StoredObjectReferenceDO;
import com.github.thundax.modules.storage.persistence.mapper.StorageMapper;
import com.github.thundax.modules.storage.persistence.mapper.StoredObjectReferenceMapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class StoredObjectDaoImpl implements StoredObjectDao {

    private static final String DEL_FLAG_COLUMN = "del_flag";
    private static final String NORMAL_DEL_FLAG = "0";
    private static final String NO_MATCH_ID = "__no_matching_storage__";

    private final StorageMapper mapper;
    private final StoredObjectReferenceMapper businessMapper;
    private final StorageCacheSupport cacheSupport;

    public StoredObjectDaoImpl(
            StorageMapper mapper, StoredObjectReferenceMapper businessMapper, StorageCacheSupport cacheSupport) {
        this.mapper = mapper;
        this.businessMapper = businessMapper;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public StoredObject getById(EntityId id) {
        StoredObject storage = cacheSupport.getById(id.value());
        if (storage != null) {
            return storage;
        }

        LambdaQueryWrapper<StorageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageDO::getId, id.value());
        wrapper.apply("del_flag = {0}", NORMAL_DEL_FLAG);
        storage = StoragePersistenceAssembler.toEntity(mapper.selectOne(wrapper));
        cacheSupport.putById(storage);
        return storage;
    }

    @Override
    public List<StoredObject> listByIds(List<String> idList) {
        List<StoredObject> storageList = new ArrayList<>();
        List<String> uncachedIdList = new ArrayList<>();
        for (String id : idList) {
            StoredObject storage = cacheSupport.getById(id);
            if (storage == null) {
                uncachedIdList.add(id);
            } else {
                storageList.add(storage);
            }
        }

        if (!uncachedIdList.isEmpty()) {
            LambdaQueryWrapper<StorageDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(StorageDO::getId, uncachedIdList);
            wrapper.apply("del_flag = {0}", NORMAL_DEL_FLAG);
            List<StoredObject> uncachedStorageList =
                    StoragePersistenceAssembler.toEntityList(mapper.selectList(wrapper));
            for (StoredObject storage : uncachedStorageList) {
                cacheSupport.putById(storage);
                storageList.add(storage);
            }
        }
        return storageList;
    }

    @Override
    public List<StoredObject> list(
            String mimeType,
            String ownerId,
            String ownerType,
            String objectStatus,
            String referenceStatus,
            String referenceOwnerId,
            String referenceOwnerType,
            String name,
            String remarks) {
        return StoragePersistenceAssembler.toEntityList(mapper.selectList(buildListWrapper(
                mimeType,
                ownerId,
                ownerType,
                objectStatus,
                referenceStatus,
                referenceOwnerId,
                referenceOwnerType,
                name,
                remarks)));
    }

    @Override
    public Page<StoredObject> page(
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
            int pageSize) {
        Page<StorageDO> dataObjectPage = mapper.selectPage(
                new Page<>(pageNo, pageSize),
                buildListWrapper(
                        mimeType,
                        ownerId,
                        ownerType,
                        objectStatus,
                        referenceStatus,
                        referenceOwnerId,
                        referenceOwnerType,
                        name,
                        remarks));
        Page<StoredObject> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(StoragePersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public String insert(StoredObject entity) {
        StorageDO dataObject = StoragePersistenceAssembler.toDataObject(entity);
        mapper.insert(dataObject);
        mapper.update(
                null,
                new UpdateWrapper<StorageDO>()
                        .set(DEL_FLAG_COLUMN, NORMAL_DEL_FLAG)
                        .eq("id", dataObject.getId()));
        cacheSupport.removeById(dataObject.getId());
        return dataObject.getId();
    }

    @Override
    public int update(StoredObject entity) {
        StorageDO dataObject = StoragePersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(StorageDO::getName, dataObject.getName())
                        .set(StorageDO::getExtendName, dataObject.getExtendName())
                        .set(StorageDO::getMimeType, dataObject.getMimeType())
                        .set(StorageDO::getOwnerId, dataObject.getOwnerId())
                        .set(StorageDO::getOwnerType, dataObject.getOwnerType())
                        .set(StorageDO::getStorageType, dataObject.getStorageType())
                        .set(StorageDO::getBucketName, dataObject.getBucketName())
                        .set(StorageDO::getObjectKey, dataObject.getObjectKey())
                        .set(StorageDO::getSize, dataObject.getSize())
                        .set(StorageDO::getAccessEndpoint, dataObject.getAccessEndpoint())
                        .set(StorageDO::getObjectStatus, dataObject.getObjectStatus())
                        .set(StorageDO::getPriority, dataObject.getPriority())
                        .set(StorageDO::getRemarks, dataObject.getRemarks()));
        cacheSupport.removeById(EntityIdCodec.toValue(entity.getId()));
        return count;
    }

    @Override
    public int deleteById(EntityId id) {
        int count = mapper.update(
                null,
                new UpdateWrapper<StorageDO>()
                        .set(DEL_FLAG_COLUMN, "1")
                        .eq("id", id.value())
                        .eq(DEL_FLAG_COLUMN, NORMAL_DEL_FLAG));
        cacheSupport.removeById(id.value());
        return count;
    }

    @Override
    public List<String> listMimeTypes() {
        return toStringList(mapper.selectObjs(new QueryWrapper<StorageDO>()
                .select("mime_type")
                .eq(DEL_FLAG_COLUMN, NORMAL_DEL_FLAG)
                .groupBy("mime_type")
                .orderByAsc("mime_type")));
    }

    @Override
    public int updateObjectStatus(StoredObject storage) {
        StorageDO dataObject = StoragePersistenceAssembler.toDataObject(storage);
        int count = mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(StorageDO::getObjectStatus, dataObject.getObjectStatus()));
        cacheSupport.removeById(EntityIdCodec.toValue(storage.getId()));
        return count;
    }

    @Override
    public int updateReferenceStatus(StoredObject storage) {
        StorageDO dataObject = StoragePersistenceAssembler.toDataObject(storage);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject).set(StorageDO::getReferenceStatus, dataObject.getReferenceStatus()));
        cacheSupport.removeById(EntityIdCodec.toValue(storage.getId()));
        return count;
    }

    private LambdaUpdateWrapper<StorageDO> buildIdUpdateWrapper(StorageDO dataObject) {
        LambdaUpdateWrapper<StorageDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(StorageDO::getId, dataObject.getId());
        return wrapper;
    }

    private LambdaQueryWrapper<StorageDO> buildListWrapper(
            String mimeType,
            String ownerId,
            String ownerType,
            String objectStatus,
            String referenceStatus,
            String referenceOwnerId,
            String referenceOwnerType,
            String name,
            String remarks) {
        LambdaQueryWrapper<StorageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("del_flag = {0}", NORMAL_DEL_FLAG);
        List<String> storageIds = findStorageIdsByBusiness(referenceOwnerId, referenceOwnerType);
        if (storageIds != null && storageIds.isEmpty()) {
            wrapper.eq(StorageDO::getId, NO_MATCH_ID);
        } else if (storageIds != null) {
            wrapper.in(StorageDO::getId, storageIds);
        }
        if (StringUtils.isNotBlank(mimeType)) {
            wrapper.eq(StorageDO::getMimeType, mimeType);
        }
        if (StringUtils.isNotBlank(ownerId)) {
            wrapper.eq(StorageDO::getOwnerId, ownerId);
        }
        if (StringUtils.isNotBlank(ownerType)) {
            wrapper.eq(StorageDO::getOwnerType, ownerType);
        }
        if (StringUtils.isNotBlank(objectStatus)) {
            wrapper.eq(StorageDO::getObjectStatus, objectStatus);
        }
        if (StringUtils.isNotBlank(referenceStatus)) {
            wrapper.eq(StorageDO::getReferenceStatus, referenceStatus);
        }
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(StorageDO::getName, name);
        }
        if (StringUtils.isNotBlank(remarks)) {
            wrapper.like(StorageDO::getRemarks, remarks);
        }
        wrapper.orderByDesc(StorageDO::getCreateDate);
        wrapper.orderByAsc(StorageDO::getPriority);
        return wrapper;
    }

    private List<String> findStorageIdsByBusiness(String referenceOwnerId, String referenceOwnerType) {
        if (StringUtils.isBlank(referenceOwnerId) && StringUtils.isBlank(referenceOwnerType)) {
            return null;
        }
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(referenceOwnerId)) {
            wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerId, referenceOwnerId);
        }
        if (StringUtils.isNotBlank(referenceOwnerType)) {
            wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerType, referenceOwnerType);
        }
        return toStringList(businessMapper.selectObjs(wrapper.select(StoredObjectReferenceDO::getFileId)));
    }

    private List<String> toStringList(List<Object> objects) {
        List<String> values = new ArrayList<>();
        if (objects == null) {
            return values;
        }
        for (Object object : objects) {
            if (object != null) {
                values.add(String.valueOf(object));
            }
        }
        return values;
    }
}
