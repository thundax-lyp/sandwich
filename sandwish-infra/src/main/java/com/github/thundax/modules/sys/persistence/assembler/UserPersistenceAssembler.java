package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.common.config.Global;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.persistence.dataobject.UserDO;
import com.github.thundax.modules.sys.persistence.dataobject.UserRoleDO;
import java.util.ArrayList;
import java.util.List;

public final class UserPersistenceAssembler {

    private UserPersistenceAssembler() {}

    public static UserDO toDataObject(User entity) {
        if (entity == null) {
            return null;
        }
        UserDO dataObject = new UserDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setDepartmentId(entity.getDepartmentId());
        dataObject.setLoginName(entity.getLoginName());
        dataObject.setLoginPass(entity.getLoginPass());
        dataObject.setEmail(entity.getEmail());
        dataObject.setMobile(entity.getMobile());
        dataObject.setTel(entity.getTel());
        dataObject.setName(entity.getName());
        dataObject.setRanks(entity.getRanks());
        dataObject.setRegisterDate(entity.getRegisterDate());
        dataObject.setRegisterIp(entity.getRegisterIp());
        dataObject.setLastLoginDate(entity.getLastLoginDate());
        dataObject.setLastLoginIp(entity.getLastLoginIp());
        dataObject.setLoginCount(entity.getLoginCount());
        dataObject.setSuperFlag(superFlag(entity.getPrivilege()));
        dataObject.setAdminFlag(adminFlag(entity.getPrivilege()));
        dataObject.setEnableFlag(statusValue(entity.getStatus()));
        dataObject.setSsoLoginName(entity.getSsoLoginName());
        dataObject.setPriority(priorityOrDefault(entity.getPriority()));
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateBy(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateBy(entity.getUpdateUserId());
        return dataObject;
    }

    public static User toEntity(UserDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        User entity = new User();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setDepartmentId(dataObject.getDepartmentId());
        entity.setLoginName(dataObject.getLoginName());
        entity.setLoginPass(dataObject.getLoginPass());
        entity.setEmail(dataObject.getEmail());
        entity.setMobile(dataObject.getMobile());
        entity.setTel(dataObject.getTel());
        entity.setName(dataObject.getName());
        entity.setRanks(dataObject.getRanks());
        entity.setRegisterDate(dataObject.getRegisterDate());
        entity.setRegisterIp(dataObject.getRegisterIp());
        entity.setLastLoginDate(dataObject.getLastLoginDate());
        entity.setLastLoginIp(dataObject.getLastLoginIp());
        entity.setLoginCount(dataObject.getLoginCount());
        entity.setPrivilege(privilegeFrom(dataObject.getSuperFlag(), dataObject.getAdminFlag()));
        entity.setStatus(statusFrom(dataObject.getEnableFlag()));
        entity.setSsoLoginName(dataObject.getSsoLoginName());
        entity.setPriority(priorityOrDefault(dataObject.getPriority()));
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateBy());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateBy());
        return entity;
    }

    public static List<User> toEntityList(List<UserDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<User> entities = new ArrayList<>();
        for (UserDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    public static UserRoleDO toUserRoleDataObject(String userId, String roleId) {
        return new UserRoleDO(userId, roleId);
    }

    private static int priorityOrDefault(Integer priority) {
        return priority == null || priority < 0 ? 0 : priority;
    }

    private static String superFlag(UserPrivilege privilege) {
        return UserPrivilege.SUPER == privilege ? Global.YES : Global.NO;
    }

    private static String adminFlag(UserPrivilege privilege) {
        return UserPrivilege.ADMIN == privilege ? Global.YES : Global.NO;
    }

    private static UserPrivilege privilegeFrom(String superFlag, String adminFlag) {
        if (Global.YES.equals(superFlag)) {
            return UserPrivilege.SUPER;
        }
        return Global.YES.equals(adminFlag) ? UserPrivilege.ADMIN : UserPrivilege.NORMAL;
    }

    private static String statusValue(UserStatus status) {
        return status == null ? null : status.value();
    }

    private static UserStatus statusFrom(String status) {
        return status == null ? null : UserStatus.from(status);
    }
}
