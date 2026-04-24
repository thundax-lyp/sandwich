package com.github.thundax.modules.storage.persistence.dao;

import com.github.thundax.modules.storage.dao.StorageDao;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.persistence.assembler.StoragePersistenceAssembler;
import com.github.thundax.modules.storage.persistence.dataobject.StorageDO;
import com.github.thundax.modules.storage.persistence.mapper.StorageMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 存储文件 DAO 实现。
 */
@Repository
public class StorageDaoImpl implements StorageDao {

    private final StorageMapper mapper;

    public StorageDaoImpl(StorageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Storage get(Storage entity) {
        return StoragePersistenceAssembler.toEntity(mapper.get(StoragePersistenceAssembler.toDataObject(entity)));
    }

    @Override
    public List<Storage> getMany(List<String> idList) {
        return StoragePersistenceAssembler.toEntityList(mapper.getMany(idList));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Storage> findList(Storage entity) {
        List<StorageDO> dataObjects = mapper.findList(StoragePersistenceAssembler.toDataObject(entity));
        List<Storage> entities = StoragePersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof com.github.pagehelper.Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(Storage entity) {
        return mapper.insert(StoragePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(Storage entity) {
        return mapper.update(StoragePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updatePriority(Storage entity) {
        return 0;
    }

    @Override
    public int updateStatus(Storage entity) {
        return 0;
    }

    @Override
    public int updateDelFlag(Storage entity) {
        return 0;
    }

    @Override
    public int delete(Storage entity) {
        return mapper.delete(StoragePersistenceAssembler.toDataObject(entity));
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
        return mapper.updateEnableFlag(StoragePersistenceAssembler.toDataObject(storage));
    }

    @Override
    public int updatePublicFlag(Storage storage) {
        return mapper.updatePublicFlag(StoragePersistenceAssembler.toDataObject(storage));
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
