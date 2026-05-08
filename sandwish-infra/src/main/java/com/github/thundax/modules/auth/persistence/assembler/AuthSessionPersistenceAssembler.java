package com.github.thundax.modules.auth.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.entity.AuthSession;
import com.github.thundax.modules.auth.entity.enums.AuthSessionStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.persistence.dataobject.AuthSessionDO;
import java.util.ArrayList;
import java.util.List;

public final class AuthSessionPersistenceAssembler {

    private AuthSessionPersistenceAssembler() {}

    public static AuthSessionDO toDataObject(AuthSession entity) {
        if (entity == null) {
            return null;
        }
        AuthSessionDO dataObject = new AuthSessionDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setSessionId(entity.getSessionId());
        dataObject.setToken(entity.getToken());
        dataObject.setUserId(EntityIdCodec.toValue(entity.getUserId()));
        dataObject.setIdentityId(EntityIdCodec.toValue(entity.getIdentityId()));
        dataObject.setIdentityType(identityTypeValue(entity.getIdentityType()));
        dataObject.setLoginType(entity.getLoginType());
        dataObject.setStatus(statusValue(entity.getStatus()));
        dataObject.setIssuedAt(entity.getIssuedAt());
        dataObject.setLastAccessTime(entity.getLastAccessTime());
        dataObject.setExpireAt(entity.getExpireAt());
        dataObject.setLogoutAt(entity.getLogoutAt());
        dataObject.setInvalidateReason(entity.getInvalidateReason());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateBy(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateBy(entity.getUpdateUserId());
        return dataObject;
    }

    public static AuthSession toEntity(AuthSessionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        AuthSession entity = new AuthSession();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setSessionId(dataObject.getSessionId());
        entity.setToken(dataObject.getToken());
        entity.setUserId(EntityIdCodec.toDomain(dataObject.getUserId()));
        entity.setIdentityId(EntityIdCodec.toDomain(dataObject.getIdentityId()));
        entity.setIdentityType(identityTypeFrom(dataObject.getIdentityType()));
        entity.setLoginType(dataObject.getLoginType());
        entity.setStatus(statusFrom(dataObject.getStatus()));
        entity.setIssuedAt(dataObject.getIssuedAt());
        entity.setLastAccessTime(dataObject.getLastAccessTime());
        entity.setExpireAt(dataObject.getExpireAt());
        entity.setLogoutAt(dataObject.getLogoutAt());
        entity.setInvalidateReason(dataObject.getInvalidateReason());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateBy());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateBy());
        return entity;
    }

    public static List<AuthSession> toEntityList(List<AuthSessionDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<AuthSession> entities = new ArrayList<>();
        for (AuthSessionDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static String identityTypeValue(PrincipalIdentityType identityType) {
        return identityType == null ? null : identityType.value();
    }

    private static PrincipalIdentityType identityTypeFrom(String identityType) {
        if (identityType == null) {
            return null;
        }
        if (identityType.startsWith("USER_")) {
            return PrincipalIdentityType.from(identityType);
        }
        return PrincipalIdentityType.from(PrincipalType.USER, identityType);
    }

    private static String statusValue(AuthSessionStatus status) {
        return status == null ? null : status.value();
    }

    private static AuthSessionStatus statusFrom(String status) {
        return status == null ? null : AuthSessionStatus.from(status);
    }
}
