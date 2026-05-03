package com.github.thundax.modules.storage.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.dao.StorageDao;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.persistence.assembler.StoragePersistenceAssembler;
import com.github.thundax.modules.storage.persistence.cache.StorageCacheSupport;
import com.github.thundax.modules.storage.persistence.dataobject.StorageBusinessDO;
import com.github.thundax.modules.storage.persistence.dataobject.StorageDO;
import com.github.thundax.modules.storage.persistence.mapper.StorageBusinessMapper;
import com.github.thundax.modules.storage.persistence.mapper.StorageMapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class StorageDaoImpl implements StorageDao {

    private static final String DEL_FLAG_COLUMN = "del_flag";
    private static final String NORMAL_DEL_FLAG = "0";
    private static final String NO_MATCH_ID = "__no_matching_storage__";

    private final StorageMapper mapper;
    private final StorageBusinessMapper businessMapper;
    private final StorageCacheSupport cacheSupport;

    public StorageDaoImpl(
            StorageMapper mapper, StorageBusinessMapper businessMapper, StorageCacheSupport cacheSupport) {
        this.mapper = mapper;
        this.businessMapper = businessMapper;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Storage getById(EntityId id) {
        Storage storage = cacheSupport.getById(id.value());
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
    public List<Storage> batchGetByIds(List<String> idList) {
        List<Storage> storageList = new ArrayList<>();
        List<String> uncachedIdList = new ArrayList<>();
        for (String id : idList) {
            Storage storage = cacheSupport.getById(id);
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
            List<Storage> uncachedStorageList = StoragePersistenceAssembler.toEntityList(mapper.selectList(wrapper));
            for (Storage storage : uncachedStorageList) {
                cacheSupport.putById(storage);
                storageList.add(storage);
            }
        }
        return storageList;
    }

    @Override
    public List<Storage> list(
            String mimeType,
            String ownerId,
            String ownerType,
            String enableFlag,
            String publicFlag,
            String businessId,
            String businessType,
            String name,
            String remarks) {
        return StoragePersistenceAssembler.toEntityList(mapper.selectList(buildListWrapper(
                mimeType, ownerId, ownerType, enableFlag, publicFlag, businessId, businessType, name, remarks)));
    }

    @Override
    public Page<Storage> page(
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
            int pageSize) {
        Page<StorageDO> dataObjectPage = mapper.selectPage(
                new Page<>(pageNo, pageSize),
                buildListWrapper(
                        mimeType, ownerId, ownerType, enableFlag, publicFlag, businessId, businessType, name, remarks));
        Page<Storage> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(StoragePersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public String insert(Storage entity) {
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
    public int update(Storage entity) {
        StorageDO dataObject = StoragePersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(StorageDO::getName, dataObject.getName())
                        .set(StorageDO::getExtendName, dataObject.getExtendName())
                        .set(StorageDO::getMimeType, dataObject.getMimeType())
                        .set(StorageDO::getOwnerId, dataObject.getOwnerId())
                        .set(StorageDO::getOwnerType, dataObject.getOwnerType())
                        .set(StorageDO::getEnableFlag, dataObject.getEnableFlag())
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
    public List<String> listBusinessTypes() {
        return toStringList(businessMapper.selectObjs(new QueryWrapper<StorageBusinessDO>()
                .select("business_type")
                .groupBy("business_type")
                .orderByAsc("business_type")));
    }

    @Override
    public int updateStatus(Storage storage) {
        StorageDO dataObject = StoragePersistenceAssembler.toDataObject(storage);
        int count = mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(StorageDO::getEnableFlag, dataObject.getEnableFlag()));
        cacheSupport.removeById(EntityIdCodec.toValue(storage.getId()));
        return count;
    }

    @Override
    public int updateVisibility(Storage storage) {
        StorageDO dataObject = StoragePersistenceAssembler.toDataObject(storage);
        int count = mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(StorageDO::getPublicFlag, dataObject.getPublicFlag()));
        cacheSupport.removeById(EntityIdCodec.toValue(storage.getId()));
        return count;
    }

    @Override
    public List<StorageBusiness> listBusiness(Storage entity) {
        LambdaQueryWrapper<StorageBusinessDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageBusinessDO::getStorageId, EntityIdCodec.toValue(entity.getId()));
        return StoragePersistenceAssembler.toBusinessEntityList(businessMapper.selectList(wrapper));
    }

    @Override
    public void insertBusiness(List<StorageBusiness> list) {
        List<StorageBusinessDO> dataObjects = StoragePersistenceAssembler.toBusinessDataObjectList(list);
        if (dataObjects == null) {
            return;
        }
        for (StorageBusinessDO dataObject : dataObjects) {
            businessMapper.insert(dataObject);
        }
    }

    @Override
    public void deleteBusiness(String id) {
        businessMapper.deleteById(id);
    }

    @Override
    public int deleteBusinessByBusiness(String businessType, String businessId) {
        LambdaQueryWrapper<StorageBusinessDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageBusinessDO::getBusinessType, businessType);
        wrapper.eq(StorageBusinessDO::getBusinessId, businessId);
        return businessMapper.delete(wrapper);
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
            String enableFlag,
            String publicFlag,
            String businessId,
            String businessType,
            String name,
            String remarks) {
        LambdaQueryWrapper<StorageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("del_flag = {0}", NORMAL_DEL_FLAG);
        List<String> storageIds = findStorageIdsByBusiness(businessId, businessType);
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
        if (StringUtils.isNotBlank(enableFlag)) {
            wrapper.eq(StorageDO::getEnableFlag, enableFlag);
        }
        if (StringUtils.isNotBlank(publicFlag)) {
            wrapper.eq(StorageDO::getPublicFlag, publicFlag);
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

    private List<String> findStorageIdsByBusiness(String businessId, String businessType) {
        if (StringUtils.isBlank(businessId) && StringUtils.isBlank(businessType)) {
            return null;
        }
        LambdaQueryWrapper<StorageBusinessDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(businessId)) {
            wrapper.eq(StorageBusinessDO::getBusinessId, businessId);
        }
        if (StringUtils.isNotBlank(businessType)) {
            wrapper.eq(StorageBusinessDO::getBusinessType, businessType);
        }
        return toStringList(businessMapper.selectObjs(wrapper.select(StorageBusinessDO::getStorageId)));
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
