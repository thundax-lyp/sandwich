package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.persistence.dataobject.MenuDO;
import java.util.ArrayList;
import java.util.List;

/** 菜单业务模型与持久化对象转换器。 */
public final class MenuPersistenceAssembler {

    private MenuPersistenceAssembler() {}

    public static MenuDO toDataObject(Menu entity) {
        if (entity == null) {
            return null;
        }
        MenuDO dataObject = new MenuDO();
        dataObject.setId(entity.getId());
        dataObject.setIsNewRecord(entity.getIsNewRecord());
        dataObject.setParentId(entity.getParentId());
        dataObject.setName(entity.getName());
        dataObject.setPerms(entity.getPerms());
        dataObject.setRanks(entity.getRanks());
        dataObject.setDisplayFlag(entity.getDisplayFlag());
        dataObject.setDisplayParams(entity.getDisplayParams());
        dataObject.setUrl(entity.getUrl());
        dataObject.setTarget(entity.getTarget());
        dataObject.setPriority(entity.getPriority());
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateUserId(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateUserId(entity.getUpdateUserId());
        dataObject.setDelFlag(entity.getDelFlag());
        return dataObject;
    }

    public static Menu toEntity(MenuDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Menu entity = new Menu();
        entity.setId(dataObject.getId());
        entity.setIsNewRecord(dataObject.getIsNewRecord());
        entity.setParentId(dataObject.getParentId());
        entity.setName(dataObject.getName());
        entity.setPerms(dataObject.getPerms());
        entity.setRanks(dataObject.getRanks());
        entity.setDisplayFlag(dataObject.getDisplayFlag());
        entity.setDisplayParams(dataObject.getDisplayParams());
        entity.setUrl(dataObject.getUrl());
        entity.setTarget(dataObject.getTarget());
        entity.setPriority(dataObject.getPriority());
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateUserId());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateUserId());
        entity.setDelFlag(dataObject.getDelFlag());
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

}
