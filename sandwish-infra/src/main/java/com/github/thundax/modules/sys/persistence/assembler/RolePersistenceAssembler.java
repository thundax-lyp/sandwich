package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.persistence.dataobject.MenuRoleDO;
import com.github.thundax.modules.sys.persistence.dataobject.RoleDO;
import com.github.thundax.modules.sys.persistence.dataobject.UserRoleDO;
import java.util.ArrayList;
import java.util.List;

public final class RolePersistenceAssembler {

    private RolePersistenceAssembler() {}

    public static RoleDO toDataObject(Role entity) {
        if (entity == null) {
            return null;
        }
        RoleDO dataObject = new RoleDO();
        dataObject.setId(entity.getId());
        dataObject.setName(entity.getName());
        dataObject.setAdminFlag(entity.getAdminFlag());
        dataObject.setEnableFlag(entity.getEnableFlag());
        dataObject.setPriority(entity.getPriority());
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateBy(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateBy(entity.getUpdateUserId());
        return dataObject;
    }

    public static Role toEntity(RoleDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Role entity = new Role();
        entity.setId(dataObject.getId());
        entity.setName(dataObject.getName());
        entity.setAdminFlag(dataObject.getAdminFlag());
        entity.setEnableFlag(dataObject.getEnableFlag());
        entity.setPriority(dataObject.getPriority());
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateBy());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateBy());
        return entity;
    }

    public static List<Role> toEntityList(List<RoleDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Role> entities = new ArrayList<>();
        for (RoleDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    public static List<User> toUserList(List<String> userIds) {
        if (userIds == null) {
            return null;
        }
        List<User> users = new ArrayList<>();
        for (String userId : userIds) {
            users.add(new User(userId));
        }
        return users;
    }

    public static List<String> toUserIdList(List<User> users) {
        if (users == null) {
            return null;
        }
        List<String> userIds = new ArrayList<>();
        for (User user : users) {
            userIds.add(user.getId());
        }
        return userIds;
    }

    public static MenuRoleDO toMenuRoleDataObject(String roleId, String menuId) {
        return new MenuRoleDO(roleId, menuId);
    }

    public static UserRoleDO toUserRoleDataObject(String userId, String roleId) {
        return new UserRoleDO(userId, roleId);
    }
}
