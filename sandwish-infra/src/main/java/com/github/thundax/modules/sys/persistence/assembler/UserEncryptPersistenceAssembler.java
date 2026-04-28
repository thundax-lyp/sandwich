package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.modules.sys.entity.UserEncrypt;
import com.github.thundax.modules.sys.persistence.dataobject.UserEncryptDO;
import java.util.ArrayList;
import java.util.List;

public final class UserEncryptPersistenceAssembler {

    private UserEncryptPersistenceAssembler() {}

    public static UserEncryptDO toDataObject(UserEncrypt entity) {
        if (entity == null) {
            return null;
        }
        UserEncryptDO dataObject = new UserEncryptDO();
        dataObject.setUserId(entity.getId());
        dataObject.setLoginPass(entity.getLoginPass());
        dataObject.setEmail(entity.getEmail());
        dataObject.setMobile(entity.getMobile());
        dataObject.setTel(entity.getTel());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateBy(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateBy(entity.getUpdateUserId());
        return dataObject;
    }

    public static UserEncrypt toEntity(UserEncryptDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        UserEncrypt entity = new UserEncrypt();
        entity.setId(dataObject.getUserId());
        entity.setLoginPass(dataObject.getLoginPass());
        entity.setEmail(dataObject.getEmail());
        entity.setMobile(dataObject.getMobile());
        entity.setTel(dataObject.getTel());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateBy());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateBy());
        return entity;
    }

    public static List<UserEncrypt> toEntityList(List<UserEncryptDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<UserEncrypt> entities = new ArrayList<>();
        for (UserEncryptDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }
}
