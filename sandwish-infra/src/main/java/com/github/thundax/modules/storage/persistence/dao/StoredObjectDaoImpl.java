package com.github.thundax.modules.storage.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.storage.dao.StoredObjectDao;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.persistence.assembler.StoragePersistenceAssembler;
import com.github.thundax.modules.storage.persistence.cache.StorageCacheSupport;
import com.github.thundax.modules.storage.persistence.dataobject.StoredObjectDO;
import com.github.thundax.modules.storage.persistence.dataobject.StoredObjectReferenceDO;
import com.github.thundax.modules.storage.persistence.mapper.StoredObjectMapper;
import com.github.thundax.modules.storage.persistence.mapper.StoredObjectReferenceMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class StoredObjectDaoImpl implements StoredObjectDao {

    private static final String DEL_FLAG_COLUMN = "del_flag";
    private static final String NORMAL_DEL_FLAG = "0";
    private static final Long NO_MATCH_ID = -1L;

    private final StoredObjectMapper mapper;
    private final StoredObjectReferenceMapper businessMapper;
    private final StorageCacheSupport cacheSupport;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public StoredObjectDaoImpl(
            StoredObjectMapper mapper, StoredObjectReferenceMapper businessMapper, StorageCacheSupport cacheSupport) {
        this.mapper = mapper;
        this.businessMapper = businessMapper;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public StoredObject getById(EntityId id) {
        StoredObject storage = cacheSupport.getById(String.valueOf(id.value()));
        if (storage != null) {
            return storage;
        }

        LambdaQueryWrapper<StoredObjectDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectDO::getId, id.value());
        wrapper.apply("del_flag = {0}", NORMAL_DEL_FLAG);
        storage = StoragePersistenceAssembler.toEntity(mapper.selectOne(wrapper));
        cacheSupport.putById(storage);
        return storage;
    }

    @Override
    public List<StoredObject> listByIds(List<Long> idList) {
        List<StoredObject> storageList = new ArrayList<>();
        List<Long> uncachedIdList = new ArrayList<>();
        for (Long id : idList) {
            StoredObject storage = cacheSupport.getById(String.valueOf(id));
            if (storage == null) {
                uncachedIdList.add(id);
            } else {
                storageList.add(storage);
            }
        }

        if (!uncachedIdList.isEmpty()) {
            LambdaQueryWrapper<StoredObjectDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(StoredObjectDO::getId, uncachedIdList);
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
        Page<StoredObjectDO> dataObjectPage = mapper.selectPage(
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
    public EntityId insert(StoredObject entity) {
        StoredObjectDO dataObject = StoragePersistenceAssembler.toDataObject(entity);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        mapper.update(
                null,
                new UpdateWrapper<StoredObjectDO>()
                        .set(DEL_FLAG_COLUMN, NORMAL_DEL_FLAG)
                        .eq("id", dataObject.getId()));
        cacheSupport.removeById(String.valueOf(dataObject.getId()));
        return EntityIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(StoredObject entity) {
        StoredObjectDO dataObject = StoragePersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(StoredObjectDO::getName, dataObject.getName())
                        .set(StoredObjectDO::getExtendName, dataObject.getExtendName())
                        .set(StoredObjectDO::getMimeType, dataObject.getMimeType())
                        .set(StoredObjectDO::getOwnerId, dataObject.getOwnerId())
                        .set(StoredObjectDO::getOwnerType, dataObject.getOwnerType())
                        .set(StoredObjectDO::getStorageType, dataObject.getStorageType())
                        .set(StoredObjectDO::getBucketName, dataObject.getBucketName())
                        .set(StoredObjectDO::getObjectKey, dataObject.getObjectKey())
                        .set(StoredObjectDO::getSize, dataObject.getSize())
                        .set(StoredObjectDO::getAccessEndpoint, dataObject.getAccessEndpoint())
                        .set(StoredObjectDO::getObjectStatus, dataObject.getObjectStatus())
                        .set(StoredObjectDO::getPriority, dataObject.getPriority())
                        .set(StoredObjectDO::getRemarks, dataObject.getRemarks()));
        cacheSupport.removeById(EntityIdCodec.toStringValue(entity.getId()));
        return count;
    }

    @Override
    public int deleteById(EntityId id) {
        int count = mapper.update(
                null,
                new UpdateWrapper<StoredObjectDO>()
                        .set(DEL_FLAG_COLUMN, "1")
                        .eq("id", id.value())
                        .eq(DEL_FLAG_COLUMN, NORMAL_DEL_FLAG));
        cacheSupport.removeById(String.valueOf(id.value()));
        return count;
    }

    @Override
    public List<String> listMimeTypes() {
        return mapper
                .selectObjs(new QueryWrapper<StoredObjectDO>()
                        .select("mime_type")
                        .eq(DEL_FLAG_COLUMN, NORMAL_DEL_FLAG)
                        .groupBy("mime_type")
                        .orderByAsc("mime_type"))
                .stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public int updateObjectStatus(StoredObject storage) {
        StoredObjectDO dataObject = StoragePersistenceAssembler.toDataObject(storage);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject).set(StoredObjectDO::getObjectStatus, dataObject.getObjectStatus()));
        cacheSupport.removeById(EntityIdCodec.toStringValue(storage.getId()));
        return count;
    }

    @Override
    public int updateReferenceStatus(StoredObject storage) {
        StoredObjectDO dataObject = StoragePersistenceAssembler.toDataObject(storage);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(StoredObjectDO::getReferenceStatus, dataObject.getReferenceStatus()));
        cacheSupport.removeById(EntityIdCodec.toStringValue(storage.getId()));
        return count;
    }

    private LambdaUpdateWrapper<StoredObjectDO> buildIdUpdateWrapper(StoredObjectDO dataObject) {
        LambdaUpdateWrapper<StoredObjectDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(StoredObjectDO::getId, dataObject.getId());
        return wrapper;
    }

    private LambdaQueryWrapper<StoredObjectDO> buildListWrapper(
            String mimeType,
            String ownerId,
            String ownerType,
            String objectStatus,
            String referenceStatus,
            String referenceOwnerId,
            String referenceOwnerType,
            String name,
            String remarks) {
        LambdaQueryWrapper<StoredObjectDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("del_flag = {0}", NORMAL_DEL_FLAG);
        List<Long> storageIds = findStorageIdsByBusiness(referenceOwnerId, referenceOwnerType);
        if (storageIds != null && storageIds.isEmpty()) {
            wrapper.eq(StoredObjectDO::getId, NO_MATCH_ID);
        } else if (storageIds != null) {
            wrapper.in(StoredObjectDO::getId, storageIds);
        }
        if (StringUtils.isNotBlank(mimeType)) {
            wrapper.eq(StoredObjectDO::getMimeType, mimeType);
        }
        if (StringUtils.isNotBlank(ownerId)) {
            wrapper.eq(StoredObjectDO::getOwnerId, ownerId);
        }
        if (StringUtils.isNotBlank(ownerType)) {
            wrapper.eq(StoredObjectDO::getOwnerType, ownerType);
        }
        if (StringUtils.isNotBlank(objectStatus)) {
            wrapper.eq(StoredObjectDO::getObjectStatus, objectStatus);
        }
        if (StringUtils.isNotBlank(referenceStatus)) {
            wrapper.eq(StoredObjectDO::getReferenceStatus, referenceStatus);
        }
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(StoredObjectDO::getName, name);
        }
        if (StringUtils.isNotBlank(remarks)) {
            wrapper.like(StoredObjectDO::getRemarks, remarks);
        }
        wrapper.orderByDesc(StoredObjectDO::getCreateDate);
        wrapper.orderByAsc(StoredObjectDO::getPriority);
        return wrapper;
    }

    private List<Long> findStorageIdsByBusiness(String referenceOwnerId, String referenceOwnerType) {
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
        return businessMapper.selectObjs(wrapper.select(StoredObjectReferenceDO::getFileId)).stream()
                .filter(Objects::nonNull)
                .map(object -> Long.valueOf(String.valueOf(object)))
                .collect(Collectors.toList());
    }
}
