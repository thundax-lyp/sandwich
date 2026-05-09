package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.modules.sys.codec.AccessRankCodec;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
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
        dataObject.setId(UserIdCodec.toValue(entity.getId()));
        dataObject.setDepartmentId(DepartmentIdCodec.toValue(entity.getDepartmentId()));
        dataObject.setEmail(entity.getEmail());
        dataObject.setMobile(entity.getMobile());
        dataObject.setTel(entity.getTel());
        dataObject.setName(entity.getName());
        dataObject.setRanks(AccessRankCodec.toValue(entity.getRank()));
        dataObject.setPrivilege(privilegeValue(entity.getPrivilege()));
        dataObject.setStatus(statusValue(entity.getStatus()));
        dataObject.setPriority(priorityOrDefault(entity.getPriority()));
        dataObject.setRemarks(entity.getRemarks());
        return dataObject;
    }

    public static User toEntity(UserDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        User entity = new User();
        entity.setId(UserIdCodec.toDomain(dataObject.getId()));
        entity.setDepartmentId(DepartmentIdCodec.toDomain(dataObject.getDepartmentId()));
        entity.setEmail(dataObject.getEmail());
        entity.setMobile(dataObject.getMobile());
        entity.setTel(dataObject.getTel());
        entity.setName(dataObject.getName());
        entity.setRank(AccessRankCodec.toDomain(dataObject.getRanks()));
        entity.setPrivilege(privilegeFrom(dataObject.getPrivilege()));
        entity.setStatus(statusFrom(dataObject.getStatus()));
        entity.setPriority(priorityOrDefault(dataObject.getPriority()));
        entity.setRemarks(dataObject.getRemarks());
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

    private static String privilegeValue(UserPrivilege privilege) {
        return privilege == null ? null : privilege.value();
    }

    private static UserPrivilege privilegeFrom(String privilege) {
        return privilege == null ? null : UserPrivilege.from(privilege);
    }

    private static String statusValue(UserStatus status) {
        return status == null ? null : status.value();
    }

    private static UserStatus statusFrom(String status) {
        return status == null ? null : UserStatus.from(status);
    }
}
