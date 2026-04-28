package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.persistence.dataobject.OfficeDO;
import java.util.ArrayList;
import java.util.List;

/** 机构业务模型与持久化对象转换器。 */
public final class OfficePersistenceAssembler {

    private OfficePersistenceAssembler() {}

    public static OfficeDO toDataObject(Office entity) {
        if (entity == null) {
            return null;
        }
        OfficeDO dataObject = new OfficeDO();
        dataObject.setId(entity.getId());
        dataObject.setIsNewRecord(entity.getIsNewRecord());
        dataObject.setParentId(entity.getParentId());
        dataObject.setName(entity.getName());
        dataObject.setShortName(entity.getShortName());
        dataObject.setPriority(entity.getPriority());
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateUserId(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateUserId(entity.getUpdateUserId());
        dataObject.setDelFlag(entity.getDelFlag());
        return dataObject;
    }

    public static Office toEntity(OfficeDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Office entity = new Office();
        entity.setId(dataObject.getId());
        entity.setIsNewRecord(dataObject.getIsNewRecord());
        entity.setParentId(dataObject.getParentId());
        entity.setName(dataObject.getName());
        entity.setShortName(dataObject.getShortName());
        entity.setPriority(dataObject.getPriority());
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateUserId());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateUserId());
        entity.setDelFlag(dataObject.getDelFlag());
        return entity;
    }

    public static List<Office> toEntityList(List<OfficeDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Office> entities = new ArrayList<>();
        for (OfficeDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

}
