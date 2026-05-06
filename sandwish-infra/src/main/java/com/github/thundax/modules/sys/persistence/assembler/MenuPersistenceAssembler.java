package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.codec.AccessRankCodec;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.persistence.dataobject.MenuDO;
import java.util.ArrayList;
import java.util.List;

public final class MenuPersistenceAssembler {

    private MenuPersistenceAssembler() {}

    public static MenuDO toDataObject(Menu entity) {
        if (entity == null) {
            return null;
        }
        MenuDO dataObject = new MenuDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setParentId(EntityIdCodec.toValue(entity.getParentId()));
        dataObject.setName(entity.getName());
        dataObject.setPerms(entity.getPerms());
        dataObject.setRanks(AccessRankCodec.toValue(entity.getRank()));
        dataObject.setDisplayFlag(visibilityValue(entity.getVisibility()));
        dataObject.setDisplayParams(entity.getDisplayParams());
        dataObject.setUrl(entity.getUrl());
        dataObject.setTarget(entity.getTarget());
        dataObject.setPriority(priorityOrDefault(entity.getPriority()));
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateBy(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateBy(entity.getUpdateUserId());
        return dataObject;
    }

    public static Menu toEntity(MenuDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Menu entity = new Menu();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setParentId(dataObject.getParentId());
        entity.setName(dataObject.getName());
        entity.setPerms(dataObject.getPerms());
        entity.setRank(AccessRankCodec.toDomain(dataObject.getRanks()));
        entity.setVisibility(visibilityFrom(dataObject.getDisplayFlag()));
        entity.setDisplayParams(dataObject.getDisplayParams());
        entity.setUrl(dataObject.getUrl());
        entity.setTarget(dataObject.getTarget());
        entity.setPriority(priorityOrDefault(dataObject.getPriority()));
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateBy());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateBy());
        return entity;
    }

    public static List<Menu> toEntityList(List<MenuDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Menu> entities = new ArrayList<>();
        for (MenuDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    public static MenuDO toParentUpdateDataObject(String id, String parentId) {
        MenuDO dataObject = new MenuDO();
        dataObject.setId(id);
        dataObject.setParentId(parentId);
        return dataObject;
    }

    private static int priorityOrDefault(Integer priority) {
        return priority == null || priority < 0 ? 0 : priority;
    }

    private static String visibilityValue(MenuVisibility visibility) {
        return visibility == null ? null : visibility.value();
    }

    private static MenuVisibility visibilityFrom(String visibility) {
        return visibility == null ? null : MenuVisibility.from(visibility);
    }
}
