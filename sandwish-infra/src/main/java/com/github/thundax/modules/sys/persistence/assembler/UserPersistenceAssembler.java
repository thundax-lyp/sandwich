package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.persistence.dataobject.UserDO;
import java.util.ArrayList;
import java.util.List;


public final class UserPersistenceAssembler {

    private UserPersistenceAssembler() {}

    public static UserDO toDataObject(User entity) {
        if (entity == null) {
            return null;
        }
        UserDO dataObject = new UserDO();
        dataObject.setId(entity.getId());
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
        dataObject.setCreateBy(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateBy(entity.getUpdateUserId());
        dataObject.setDelFlag(entity.getDelFlag());
        return dataObject;
    }

    public static User toEntity(UserDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        User entity = new User();
        entity.setId(dataObject.getId());
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
        entity.setCreateUserId(dataObject.getCreateBy());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateBy());
        entity.setDelFlag(dataObject.getDelFlag());
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

    public static List<Role> toRoleList(List<String> roleIds) {
        if (roleIds == null) {
            return null;
        }
        List<Role> roles = new ArrayList<>();
        for (String roleId : roleIds) {
            roles.add(new Role(roleId));
        }
        return roles;
    }
}
