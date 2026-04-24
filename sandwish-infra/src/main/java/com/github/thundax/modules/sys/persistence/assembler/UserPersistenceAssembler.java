package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.persistence.dataobject.UserDO;

import java.util.List;

/**
 * 用户业务模型与持久化对象转换器。
 */
public final class UserPersistenceAssembler {

    private UserPersistenceAssembler() {
    }

    public static UserDO toDataObject(User entity) {
        if (entity == null) {
            return null;
        }
        UserDO dataObject = new UserDO();
        dataObject.setId(entity.getId());
        dataObject.setIsNewRecord(entity.getIsNewRecord());
        dataObject.setOfficeId(entity.getOfficeId());
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
        dataObject.setSuperFlag(entity.getSuperFlag());
        dataObject.setAdminFlag(entity.getAdminFlag());
        dataObject.setEnableFlag(entity.getEnableFlag());
        dataObject.setSsoLoginName(entity.getSsoLoginName());
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

    public static UserDO toDataObjectWithRoles(User entity) {
        UserDO dataObject = toDataObject(entity);
        if (dataObject != null) {
            dataObject.setRoleIdList(entity.getRoleIdList());
        }
        return dataObject;
    }

    public static User toEntity(UserDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        User entity = new User();
        entity.setId(dataObject.getId());
        entity.setIsNewRecord(dataObject.getIsNewRecord());
        entity.setOfficeId(dataObject.getOfficeId());
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
        entity.setSuperFlag(dataObject.getSuperFlag());
        entity.setAdminFlag(dataObject.getAdminFlag());
        entity.setEnableFlag(dataObject.getEnableFlag());
        entity.setSsoLoginName(dataObject.getSsoLoginName());
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

    public static List<User> toEntityList(List<UserDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<User> entities = ListUtils.newArrayList();
        for (UserDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    public static List<Role> toRoleList(List<String> roleIds) {
        if (roleIds == null) {
            return null;
        }
        List<Role> roles = ListUtils.newArrayList();
        for (String roleId : roleIds) {
            roles.add(new Role(roleId));
        }
        return roles;
    }

    private static UserDO.Query toDataObjectQuery(User.Query query) {
        if (query == null) {
            return null;
        }
        UserDO.Query dataObjectQuery = new UserDO.Query();
        dataObjectQuery.setOfficeId(query.getOfficeId());
        dataObjectQuery.setOfficeTreeLeft(query.getOfficeTreeLeft());
        dataObjectQuery.setOfficeTreeRight(query.getOfficeTreeRight());
        dataObjectQuery.setLoginName(query.getLoginName());
        dataObjectQuery.setName(query.getName());
        dataObjectQuery.setEnableFlag(query.getEnableFlag());
        dataObjectQuery.setSuperFlag(query.getSuperFlag());
        dataObjectQuery.setOrderBy(query.getOrderBy());
        return dataObjectQuery;
    }

    private static User.Query toEntityQuery(UserDO.Query query) {
        if (query == null) {
            return null;
        }
        User.Query entityQuery = new User.Query();
        entityQuery.setOfficeId(query.getOfficeId());
        entityQuery.setOfficeTreeLeft(query.getOfficeTreeLeft());
        entityQuery.setOfficeTreeRight(query.getOfficeTreeRight());
        entityQuery.setLoginName(query.getLoginName());
        entityQuery.setName(query.getName());
        entityQuery.setEnableFlag(query.getEnableFlag());
        entityQuery.setSuperFlag(query.getSuperFlag());
        entityQuery.setOrderBy(query.getOrderBy());
        return entityQuery;
    }
}
