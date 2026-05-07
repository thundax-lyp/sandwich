package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.codec.AccessRankCodec;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.persistence.dataobject.UserDO;
import com.github.thundax.modules.sys.persistence.dataobject.UserRoleDO;
import java.util.ArrayList;
import java.util.List;

public final class UserPersistenceAssembler {

    private static final String LEGACY_YES = "1";
    private static final String LEGACY_NO = "0";

    private UserPersistenceAssembler() {}

    public static UserDO toDataObject(User entity) {
        if (entity == null) {
            return null;
        }
        UserDO dataObject = new UserDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setDepartmentId(entity.getDepartmentId());
        dataObject.setEmail(entity.getEmail());
        dataObject.setMobile(entity.getMobile());
        dataObject.setTel(entity.getTel());
        dataObject.setName(entity.getName());
        dataObject.setRanks(AccessRankCodec.toValue(entity.getRank()));
        dataObject.setSuperFlag(superFlag(entity.getPrivilege()));
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

    public static User toEntity(UserDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        User entity = new User();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setDepartmentId(dataObject.getDepartmentId());
        entity.setEmail(dataObject.getEmail());
        entity.setMobile(dataObject.getMobile());
        entity.setTel(dataObject.getTel());
        entity.setName(dataObject.getName());
        entity.setRank(AccessRankCodec.toDomain(dataObject.getRanks()));
        entity.setPrivilege(privilegeFrom(dataObject.getSuperFlag(), dataObject.getAdminFlag()));
        entity.setStatus(statusFrom(dataObject.getEnableFlag()));
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

    public static UserRoleDO toUserRoleDataObject(Long userId, Long roleId) {
        return new UserRoleDO(userId, roleId);
    }

    private static int priorityOrDefault(Integer priority) {
        return priority == null || priority < 0 ? 0 : priority;
    }

    private static String superFlag(UserPrivilege privilege) {
        return UserPrivilege.SUPER == privilege ? LEGACY_YES : LEGACY_NO;
    }

    private static String adminFlag(UserPrivilege privilege) {
        return UserPrivilege.ADMIN == privilege ? LEGACY_YES : LEGACY_NO;
    }

    private static UserPrivilege privilegeFrom(String superFlag, String adminFlag) {
        if (LEGACY_YES.equals(superFlag)) {
            return UserPrivilege.SUPER;
        }
        return LEGACY_YES.equals(adminFlag) ? UserPrivilege.ADMIN : UserPrivilege.NORMAL;
    }

    private static String statusValue(UserStatus status) {
        return status == null ? null : status.value();
    }

    private static UserStatus statusFrom(String status) {
        return status == null ? null : UserStatus.from(status);
    }
}
