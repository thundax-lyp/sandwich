package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.enums.RolePrivilege;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.persistence.dataobject.MenuRoleDO;
import com.github.thundax.modules.sys.persistence.dataobject.RoleDO;
import com.github.thundax.modules.sys.persistence.dataobject.UserRoleDO;
import java.util.ArrayList;
import java.util.List;

public final class RolePersistenceAssembler {

    private static final String LEGACY_YES = "1";
    private static final String LEGACY_NO = "0";

    private RolePersistenceAssembler() {}

    public static RoleDO toDataObject(Role entity) {
        if (entity == null) {
            return null;
        }
        RoleDO dataObject = new RoleDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setName(entity.getName());
        dataObject.setAdminFlag(adminFlag(entity.getPrivilege()));
        dataObject.setEnableFlag(statusValue(entity.getStatus()));
        dataObject.setPriority(priorityOrDefault(entity.getPriority()));
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
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setName(dataObject.getName());
        entity.setPrivilege(privilegeFrom(dataObject.getAdminFlag()));
        entity.setStatus(statusFrom(dataObject.getEnableFlag()));
        entity.setPriority(priorityOrDefault(dataObject.getPriority()));
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

    public static MenuRoleDO toMenuRoleDataObject(String roleId, String menuId) {
        return new MenuRoleDO(roleId, menuId);
    }

    public static UserRoleDO toUserRoleDataObject(String userId, String roleId) {
        return new UserRoleDO(userId, roleId);
    }

    private static int priorityOrDefault(Integer priority) {
        return priority == null || priority < 0 ? 0 : priority;
    }

    private static String adminFlag(RolePrivilege privilege) {
        return RolePrivilege.ADMIN == privilege ? LEGACY_YES : LEGACY_NO;
    }

    private static RolePrivilege privilegeFrom(String adminFlag) {
        return LEGACY_YES.equals(adminFlag) ? RolePrivilege.ADMIN : RolePrivilege.NORMAL;
    }

    private static String statusValue(RoleStatus status) {
        return status == null ? null : status.value();
    }

    private static RoleStatus statusFrom(String status) {
        return status == null ? null : RoleStatus.from(status);
    }
}
