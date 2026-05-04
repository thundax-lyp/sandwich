package com.github.thundax.modules.auth.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.entity.UserCredential;
import com.github.thundax.modules.auth.entity.enums.UserCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.UserCredentialType;
import com.github.thundax.modules.auth.persistence.dataobject.UserCredentialDO;
import java.util.ArrayList;
import java.util.List;

public final class UserCredentialPersistenceAssembler {

    private UserCredentialPersistenceAssembler() {}

    public static UserCredentialDO toDataObject(UserCredential entity) {
        if (entity == null) {
            return null;
        }
        UserCredentialDO dataObject = new UserCredentialDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setUserId(EntityIdCodec.toValue(entity.getUserId()));
        dataObject.setIdentityId(EntityIdCodec.toValue(entity.getIdentityId()));
        dataObject.setCredentialType(credentialTypeValue(entity.getCredentialType()));
        dataObject.setCredentialValue(entity.getCredentialValue());
        dataObject.setStatus(statusValue(entity.getStatus()));
        dataObject.setNeedChangePassword(entity.isNeedChangePassword());
        dataObject.setFailedCount(entity.getFailedCount());
        dataObject.setFailedLimit(entity.getFailedLimit());
        dataObject.setLockedUntil(entity.getLockedUntil());
        dataObject.setExpiresAt(entity.getExpiresAt());
        dataObject.setLastVerifiedAt(entity.getLastVerifiedAt());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateBy(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateBy(entity.getUpdateUserId());
        return dataObject;
    }

    public static UserCredential toEntity(UserCredentialDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        UserCredential entity = new UserCredential();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setUserId(EntityIdCodec.toDomain(dataObject.getUserId()));
        entity.setIdentityId(EntityIdCodec.toDomain(dataObject.getIdentityId()));
        entity.setCredentialType(credentialTypeFrom(dataObject.getCredentialType()));
        entity.setCredentialValue(dataObject.getCredentialValue());
        entity.setStatus(statusFrom(dataObject.getStatus()));
        entity.setNeedChangePassword(booleanOrFalse(dataObject.getNeedChangePassword()));
        entity.setFailedCount(intOrZero(dataObject.getFailedCount()));
        entity.setFailedLimit(intOrZero(dataObject.getFailedLimit()));
        entity.setLockedUntil(dataObject.getLockedUntil());
        entity.setExpiresAt(dataObject.getExpiresAt());
        entity.setLastVerifiedAt(dataObject.getLastVerifiedAt());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateBy());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateBy());
        return entity;
    }

    public static List<UserCredential> toEntityList(List<UserCredentialDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<UserCredential> entities = new ArrayList<>();
        for (UserCredentialDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static int intOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static boolean booleanOrFalse(Boolean value) {
        return value != null && value;
    }

    private static String credentialTypeValue(UserCredentialType credentialType) {
        return credentialType == null ? null : credentialType.value();
    }

    private static UserCredentialType credentialTypeFrom(String credentialType) {
        return credentialType == null ? null : UserCredentialType.from(credentialType);
    }

    private static String statusValue(UserCredentialStatus status) {
        return status == null ? null : status.value();
    }

    private static UserCredentialStatus statusFrom(String status) {
        return status == null ? null : UserCredentialStatus.from(status);
    }
}
