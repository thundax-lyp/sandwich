package com.github.thundax.modules.auth.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.entity.UserIdentity;
import com.github.thundax.modules.auth.entity.enums.UserIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.UserIdentityType;
import com.github.thundax.modules.auth.persistence.dataobject.UserIdentityDO;
import java.util.ArrayList;
import java.util.List;

public final class UserIdentityPersistenceAssembler {

    private UserIdentityPersistenceAssembler() {}

    public static UserIdentityDO toDataObject(UserIdentity entity) {
        if (entity == null) {
            return null;
        }
        UserIdentityDO dataObject = new UserIdentityDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setUserId(EntityIdCodec.toValue(entity.getUserId()));
        dataObject.setIdentityType(identityTypeValue(entity.getIdentityType()));
        dataObject.setIdentityValue(entity.getIdentityValue());
        dataObject.setStatus(statusValue(entity.getStatus()));
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateBy(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateBy(entity.getUpdateUserId());
        return dataObject;
    }

    public static UserIdentity toEntity(UserIdentityDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        UserIdentity entity = new UserIdentity();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setUserId(EntityIdCodec.toDomain(dataObject.getUserId()));
        entity.setIdentityType(identityTypeFrom(dataObject.getIdentityType()));
        entity.setIdentityValue(dataObject.getIdentityValue());
        entity.setStatus(statusFrom(dataObject.getStatus()));
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateBy());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateBy());
        return entity;
    }

    public static List<UserIdentity> toEntityList(List<UserIdentityDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<UserIdentity> entities = new ArrayList<>();
        for (UserIdentityDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static String identityTypeValue(UserIdentityType identityType) {
        return identityType == null ? null : identityType.value();
    }

    private static UserIdentityType identityTypeFrom(String identityType) {
        return identityType == null ? null : UserIdentityType.from(identityType);
    }

    private static String statusValue(UserIdentityStatus status) {
        return status == null ? null : status.value();
    }

    private static UserIdentityStatus statusFrom(String status) {
        return status == null ? null : UserIdentityStatus.from(status);
    }
}
