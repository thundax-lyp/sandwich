package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.persistence.dataobject.DepartmentDO;
import java.util.ArrayList;
import java.util.List;

public final class DepartmentPersistenceAssembler {

    private DepartmentPersistenceAssembler() {}

    public static DepartmentDO toDataObject(Department entity) {
        if (entity == null) {
            return null;
        }
        DepartmentDO dataObject = new DepartmentDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setParentId(entity.getParentId());
        dataObject.setName(entity.getName());
        dataObject.setShortName(entity.getShortName());
        dataObject.setPriority(priorityOrDefault(entity.getPriority()));
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateBy(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateBy(entity.getUpdateUserId());
        return dataObject;
    }

    public static Department toEntity(DepartmentDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Department entity = new Department();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setParentId(dataObject.getParentId());
        entity.setName(dataObject.getName());
        entity.setShortName(dataObject.getShortName());
        entity.setPriority(priorityOrDefault(dataObject.getPriority()));
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateBy());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateBy());
        return entity;
    }

    public static List<Department> toEntityList(List<DepartmentDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Department> entities = new ArrayList<>();
        for (DepartmentDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    public static DepartmentDO toParentUpdateDataObject(Long id, Long parentId) {
        DepartmentDO dataObject = new DepartmentDO();
        dataObject.setId(id);
        dataObject.setParentId(parentId);
        return dataObject;
    }

    private static int priorityOrDefault(Integer priority) {
        return priority == null || priority < 0 ? 0 : priority;
    }
}
