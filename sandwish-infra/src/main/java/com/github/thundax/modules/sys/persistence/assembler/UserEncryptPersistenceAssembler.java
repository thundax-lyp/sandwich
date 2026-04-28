package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.modules.sys.entity.UserEncrypt;
import com.github.thundax.modules.sys.persistence.dataobject.UserEncryptDO;
import java.util.ArrayList;
import java.util.List;

/** 用户加密信息业务模型与持久化对象转换器。 */
public final class UserEncryptPersistenceAssembler {

    private UserEncryptPersistenceAssembler() {}

    public static UserEncryptDO toDataObject(UserEncrypt entity) {
        if (entity == null) {
            return null;
        }
        UserEncryptDO dataObject = new UserEncryptDO();
        dataObject.setId(entity.getId());
        dataObject.setLoginPass(entity.getLoginPass());
        dataObject.setEmail(entity.getEmail());
        dataObject.setMobile(entity.getMobile());
        dataObject.setTel(entity.getTel());
        dataObject.setPriority(entity.getPriority());
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateUserId(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateUserId(entity.getUpdateUserId());
        dataObject.setDelFlag(entity.getDelFlag());
        return dataObject;
    }

    public static UserEncrypt toEntity(UserEncryptDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        UserEncrypt entity = new UserEncrypt();
        entity.setId(dataObject.getId());
        entity.setLoginPass(dataObject.getLoginPass());
        entity.setEmail(dataObject.getEmail());
        entity.setMobile(dataObject.getMobile());
        entity.setTel(dataObject.getTel());
        entity.setPriority(dataObject.getPriority());
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateUserId());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateUserId());
        entity.setDelFlag(dataObject.getDelFlag());
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
