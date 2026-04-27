package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.persistence.dataobject.DictDO;
import java.util.ArrayList;
import java.util.List;

/** 字典业务模型与持久化对象转换器。 */
public final class DictPersistenceAssembler {

    private DictPersistenceAssembler() {}

    public static DictDO toDataObject(Dict entity) {
        if (entity == null) {
            return null;
        }
        DictDO dataObject = new DictDO();
        dataObject.setId(entity.getId());
        dataObject.setIsNewRecord(entity.getIsNewRecord());
        dataObject.setType(entity.getType());
        dataObject.setLabel(entity.getLabel());
        dataObject.setValue(entity.getValue());
        dataObject.setPriority(entity.getPriority());
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateUserId(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateUserId(entity.getUpdateUserId());
        dataObject.setDelFlag(entity.getDelFlag());
        copyQuery(entity.getQuery(), dataObject);
        return dataObject;
    }

    public static Dict toEntity(DictDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Dict entity = new Dict();
        entity.setId(dataObject.getId());
        entity.setIsNewRecord(dataObject.getIsNewRecord());
        entity.setType(dataObject.getType());
        entity.setLabel(dataObject.getLabel());
        entity.setValue(dataObject.getValue());
        entity.setPriority(dataObject.getPriority());
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateUserId());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateUserId());
        entity.setDelFlag(dataObject.getDelFlag());
        return entity;
    }

    public static List<Dict> toEntityList(List<DictDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Dict> entities = new ArrayList<>();
        for (DictDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static void copyQuery(Dict.Query query, DictDO dataObject) {
        if (query == null) {
            return;
        }
        dataObject.setQueryType(query.getType());
        dataObject.setQueryRemarks(query.getRemarks());
        dataObject.setQueryLabel(query.getLabel());
    }
}
