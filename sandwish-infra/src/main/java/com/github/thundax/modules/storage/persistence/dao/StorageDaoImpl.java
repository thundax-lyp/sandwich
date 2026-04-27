package com.github.thundax.modules.storage.persistence.dao;

import com.github.pagehelper.Page;
import com.github.thundax.modules.storage.dao.StorageDao;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.persistence.assembler.StoragePersistenceAssembler;
import com.github.thundax.modules.storage.persistence.cache.StorageCacheSupport;
import com.github.thundax.modules.storage.persistence.dataobject.StorageDO;
import com.github.thundax.modules.storage.persistence.mapper.StorageMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

/** 存储文件 DAO 实现。 */
@Repository
public class StorageDaoImpl implements StorageDao {

    private final StorageMapper mapper;
    private final StorageCacheSupport cacheSupport;

    public StorageDaoImpl(StorageMapper mapper, StorageCacheSupport cacheSupport) {
        this.mapper = mapper;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Storage get(Storage entity) {
        Storage storage = cacheSupport.getById(entity.getId());
        if (storage != null) {
            return storage;
        }

        storage =
                StoragePersistenceAssembler.toEntity(
                        mapper.get(StoragePersistenceAssembler.toDataObject(entity)));
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
                    StoragePersistenceAssembler.toEntityList(mapper.getMany(uncachedIdList));
            for (Storage storage : uncachedStorageList) {
                cacheSupport.putById(storage);
                storageList.add(storage);
            }
        }
        return storageList;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Storage> findList(Storage entity) {
        List<StorageDO> dataObjects =
                mapper.findList(StoragePersistenceAssembler.toDataObject(entity));
        List<Storage> entities = StoragePersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(Storage entity) {
        int count = mapper.insert(StoragePersistenceAssembler.toDataObject(entity));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public int update(Storage entity) {
        int count = mapper.update(StoragePersistenceAssembler.toDataObject(entity));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public int updatePriority(Storage entity) {
        cacheSupport.removeById(entity.getId());
        return 0;
    }

    public int updateStatus(Storage entity) {
        return 0;
    }

    @Override
    public int updateDelFlag(Storage entity) {
        cacheSupport.removeById(entity.getId());
        return 0;
    }

    @Override
    public int delete(Storage entity) {
        int count = mapper.delete(StoragePersistenceAssembler.toDataObject(entity));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public List<String> findMimeTypeList() {
        return mapper.findMimeTypeList();
    }

    @Override
    public List<String> findBusinessTypeList() {
        return mapper.findBusinessTypeList();
    }

    @Override
    public int updateEnableFlag(Storage storage) {
        int count = mapper.updateEnableFlag(StoragePersistenceAssembler.toDataObject(storage));
        cacheSupport.removeById(storage.getId());
        return count;
    }

    @Override
    public int updatePublicFlag(Storage storage) {
        int count = mapper.updatePublicFlag(StoragePersistenceAssembler.toDataObject(storage));
        cacheSupport.removeById(storage.getId());
        return count;
    }

    @Override
    public List<StorageBusiness> findBusiness(Storage entity) {
        return StoragePersistenceAssembler.toBusinessEntityList(
                mapper.findBusiness(StoragePersistenceAssembler.toDataObject(entity)));
    }

    @Override
    public void insertBusiness(List<StorageBusiness> list) {
        mapper.insertBusiness(StoragePersistenceAssembler.toBusinessDataObjectList(list));
    }

    @Override
    public void deleteBusiness(Storage entity) {
        mapper.deleteBusiness(StoragePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int deleteBusinessByBusiness(String businessType, String businessId) {
        return mapper.deleteBusinessByBusiness(businessType, businessId);
    }
}
