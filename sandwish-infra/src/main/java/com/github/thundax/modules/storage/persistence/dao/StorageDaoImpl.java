package com.github.thundax.modules.storage.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

/** 存储文件 DAO 实现。 */
@Repository
public class StorageDaoImpl implements StorageDao {

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
    public Storage get(String id) {
        Storage storage = cacheSupport.getById(id);
        if (storage != null) {
            return storage;
        }

        storage = StoragePersistenceAssembler.toEntity(mapper.selectById(id));
        cacheSupport.putById(storage);
        return storage;
    }

    @Override
    public List<Storage> getMany(List<String> idList) {
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
            List<Storage> uncachedStorageList =
                    StoragePersistenceAssembler.toEntityList(mapper.selectBatchIds(uncachedIdList));
            for (Storage storage : uncachedStorageList) {
                cacheSupport.putById(storage);
                storageList.add(storage);
            }
        }
        return storageList;
    }

    @Override
    public List<Storage> findList(
            String mimeType, String ownerId, String ownerType, String enableFlag, String name, String remarks) {
        return StoragePersistenceAssembler.toEntityList(
                mapper.selectList(buildListWrapper(mimeType, ownerId, ownerType, enableFlag, name, remarks)));
    }

    @Override
    public Page<Storage> findPage(
            String mimeType,
            String ownerId,
            String ownerType,
            String enableFlag,
            String name,
            String remarks,
            int pageNo,
            int pageSize) {
        Page<StorageDO> dataObjectPage = mapper.selectPage(
                new Page<>(pageNo, pageSize),
                buildListWrapper(mimeType, ownerId, ownerType, enableFlag, name, remarks));
        Page<Storage> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(StoragePersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public String insert(Storage entity) {
        StorageDO dataObject = StoragePersistenceAssembler.toDataObject(entity);
        mapper.insert(dataObject);
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
                        .set(StorageDO::getRemarks, dataObject.getRemarks())
                        .set(StorageDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(StorageDO::getDelFlag, dataObject.getDelFlag()));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public int delete(String id) {
        int count = mapper.deleteById(id);
        cacheSupport.removeById(id);
        return count;
    }

    @Override
    public List<String> findMimeTypeList() {
        return toStringList(mapper.selectObjs(new QueryWrapper<StorageDO>()
                .select("mime_type")
                .groupBy("mime_type")
                .orderByAsc("mime_type")));
    }

    @Override
    public List<String> findBusinessTypeList() {
        return toStringList(businessMapper.selectObjs(new QueryWrapper<StorageBusinessDO>()
                .select("business_type")
                .groupBy("business_type")
                .orderByAsc("business_type")));
    }

    @Override
    public int updateEnableFlag(Storage storage) {
        StorageDO dataObject = StoragePersistenceAssembler.toDataObject(storage);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(StorageDO::getEnableFlag, dataObject.getEnableFlag())
                        .set(StorageDO::getUpdateDate, dataObject.getUpdateDate()));
        cacheSupport.removeById(storage.getId());
        return count;
    }

    @Override
    public int updatePublicFlag(Storage storage) {
        StorageDO dataObject = StoragePersistenceAssembler.toDataObject(storage);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(StorageDO::getPublicFlag, dataObject.getPublicFlag())
                        .set(StorageDO::getUpdateDate, dataObject.getUpdateDate()));
        cacheSupport.removeById(storage.getId());
        return count;
    }

    @Override
    public List<StorageBusiness> findBusiness(Storage entity) {
        LambdaQueryWrapper<StorageBusinessDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageBusinessDO::getStorageId, entity.getId());
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
            String mimeType, String ownerId, String ownerType, String enableFlag, String name, String remarks) {
        LambdaQueryWrapper<StorageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageDO::getDelFlag, StorageDO.DEL_FLAG_NORMAL);
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
