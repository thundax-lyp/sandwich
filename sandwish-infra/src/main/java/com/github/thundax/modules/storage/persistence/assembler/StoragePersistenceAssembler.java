package com.github.thundax.modules.storage.persistence.assembler;

import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.persistence.dataobject.StorageBusinessDO;
import com.github.thundax.modules.storage.persistence.dataobject.StorageDO;
import java.util.ArrayList;
import java.util.List;

/** 存储业务模型与持久化对象转换器。 */
public final class StoragePersistenceAssembler {

    private StoragePersistenceAssembler() {}

    public static StorageDO toDataObject(Storage entity) {
        if (entity == null) {
            return null;
        }
        StorageDO dataObject = new StorageDO();
        dataObject.setId(entity.getId());
        dataObject.setIsNewRecord(entity.getIsNewRecord());
        dataObject.setName(entity.getName());
        dataObject.setExtendName(entity.getExtendName());
        dataObject.setMimeType(entity.getMimeType());
        dataObject.setOwnerId(entity.getOwnerId());
        dataObject.setOwnerType(entity.getOwnerType());
        dataObject.setEnableFlag(entity.getEnableFlag());
        dataObject.setPriority(entity.getPriority());
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setDelFlag(entity.getDelFlag());
        return dataObject;
    }

    public static Storage toEntity(StorageDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Storage entity = new Storage();
        entity.setId(dataObject.getId());
        entity.setIsNewRecord(dataObject.getIsNewRecord());
        entity.setName(dataObject.getName());
        entity.setExtendName(dataObject.getExtendName());
        entity.setMimeType(dataObject.getMimeType());
        entity.setOwnerId(dataObject.getOwnerId());
        entity.setOwnerType(dataObject.getOwnerType());
        entity.setEnableFlag(dataObject.getEnableFlag());
        entity.setPriority(dataObject.getPriority());
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setDelFlag(dataObject.getDelFlag());
        return entity;
    }

    public static List<Storage> toEntityList(List<StorageDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Storage> entities = new ArrayList<>();
        for (StorageDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    public static StorageBusinessDO toBusinessDataObject(StorageBusiness entity) {
        if (entity == null) {
            return null;
        }
        StorageBusinessDO dataObject = new StorageBusinessDO();
        dataObject.setId(entity.getId());
        dataObject.setIsNewRecord(entity.getIsNewRecord());
        dataObject.setBusinessId(entity.getBusinessId());
        dataObject.setBusinessType(entity.getBusinessType());
        dataObject.setBusinessParams(entity.getBusinessParams());
        dataObject.setPublicFlag(entity.getPublicFlag());
        dataObject.setPriority(entity.getPriority());
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setDelFlag(entity.getDelFlag());
        return dataObject;
    }

    public static StorageBusiness toBusinessEntity(StorageBusinessDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        StorageBusiness entity = new StorageBusiness();
        entity.setId(dataObject.getId());
        entity.setIsNewRecord(dataObject.getIsNewRecord());
        entity.setBusinessId(dataObject.getBusinessId());
        entity.setBusinessType(dataObject.getBusinessType());
        entity.setBusinessParams(dataObject.getBusinessParams());
        entity.setPublicFlag(dataObject.getPublicFlag());
        entity.setPriority(dataObject.getPriority());
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setDelFlag(dataObject.getDelFlag());
        return entity;
    }

    public static List<StorageBusinessDO> toBusinessDataObjectList(List<StorageBusiness> entities) {
        if (entities == null) {
            return null;
        }
        List<StorageBusinessDO> dataObjects = new ArrayList<>();
        for (StorageBusiness entity : entities) {
            dataObjects.add(toBusinessDataObject(entity));
        }
        return dataObjects;
    }

    public static List<StorageBusiness> toBusinessEntityList(List<StorageBusinessDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<StorageBusiness> entities = new ArrayList<>();
        for (StorageBusinessDO dataObject : dataObjects) {
            entities.add(toBusinessEntity(dataObject));
        }
        return entities;
    }
}
