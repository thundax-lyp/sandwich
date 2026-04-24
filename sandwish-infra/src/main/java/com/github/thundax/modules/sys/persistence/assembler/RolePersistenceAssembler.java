package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.persistence.dataobject.RoleDO;

import java.util.List;

/**
 * 角色业务模型与持久化对象转换器。
 */
public final class RolePersistenceAssembler {

    private RolePersistenceAssembler() {
    }

    public static RoleDO toDataObject(Role entity) {
        if (entity == null) {
            return null;
        }
        RoleDO dataObject = new RoleDO();
        dataObject.setId(entity.getId());
        dataObject.setIsNewRecord(entity.getIsNewRecord());
        dataObject.setName(entity.getName());
        dataObject.setAdminFlag(entity.getAdminFlag());
        dataObject.setEnableFlag(entity.getEnableFlag());
        dataObject.setPriority(entity.getPriority());
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateUserId(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateUserId(entity.getUpdateUserId());
        dataObject.setDelFlag(entity.getDelFlag());
        dataObject.setQuery(toDataObjectQuery(entity.getQuery()));
        return dataObject;
    }

    public static RoleDO toDataObjectWithMenus(Role entity) {
        RoleDO dataObject = toDataObject(entity);
        if (dataObject != null) {
            dataObject.setMenuIdList(entity.getMenuIdList());
        }
        return dataObject;
    }

    public static Role toEntity(RoleDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Role entity = new Role();
        entity.setId(dataObject.getId());
        entity.setIsNewRecord(dataObject.getIsNewRecord());
        entity.setName(dataObject.getName());
        entity.setAdminFlag(dataObject.getAdminFlag());
        entity.setEnableFlag(dataObject.getEnableFlag());
        entity.setMenuIdList(dataObject.getMenuIdList());
        entity.setPriority(dataObject.getPriority());
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateUserId());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateUserId());
        entity.setDelFlag(dataObject.getDelFlag());
        entity.setQuery(toEntityQuery(dataObject.getQuery()));
        return entity;
    }

    public static List<Role> toEntityList(List<RoleDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Role> entities = ListUtils.newArrayList();
        for (RoleDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    public static List<User> toUserList(List<String> userIds) {
        if (userIds == null) {
            return null;
        }
        List<User> users = ListUtils.newArrayList();
        for (String userId : userIds) {
            users.add(new User(userId));
        }
        return users;
    }

    public static List<String> toUserIdList(List<User> users) {
        if (users == null) {
            return null;
        }
        List<String> userIds = ListUtils.newArrayList();
        for (User user : users) {
            userIds.add(user.getId());
        }
        return userIds;
    }

    private static RoleDO.Query toDataObjectQuery(Role.Query query) {
        if (query == null) {
            return null;
        }
        RoleDO.Query dataObjectQuery = new RoleDO.Query();
        dataObjectQuery.setEnableFlag(query.getEnableFlag());
        return dataObjectQuery;
    }

    private static Role.Query toEntityQuery(RoleDO.Query query) {
        if (query == null) {
            return null;
        }
        Role.Query entityQuery = new Role.Query();
        entityQuery.setEnableFlag(query.getEnableFlag());
        return entityQuery;
    }
}
