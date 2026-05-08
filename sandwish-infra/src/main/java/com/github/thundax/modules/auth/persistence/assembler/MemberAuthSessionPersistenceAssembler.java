package com.github.thundax.modules.auth.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.entity.MemberAuthSession;
import com.github.thundax.modules.auth.entity.enums.MemberAuthSessionStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.persistence.dataobject.MemberAuthSessionDO;
import java.util.ArrayList;
import java.util.List;

public final class MemberAuthSessionPersistenceAssembler {
    private MemberAuthSessionPersistenceAssembler() {}

    public static MemberAuthSessionDO toDataObject(MemberAuthSession entity) {
        if (entity == null) {
            return null;
        }
        MemberAuthSessionDO dataObject = new MemberAuthSessionDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setSessionId(entity.getSessionId());
        dataObject.setMemberId(EntityIdCodec.toValue(entity.getMemberId()));
        dataObject.setIdentityId(EntityIdCodec.toValue(entity.getIdentityId()));
        dataObject.setIdentityType(identityTypeValue(entity.getIdentityType()));
        dataObject.setLoginType(entity.getLoginType());
        dataObject.setStatus(
                entity.getStatus() == null ? null : entity.getStatus().value());
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

    public static MemberAuthSession toEntity(MemberAuthSessionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        MemberAuthSession entity = new MemberAuthSession();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setSessionId(dataObject.getSessionId());
        entity.setMemberId(EntityIdCodec.toDomain(dataObject.getMemberId()));
        entity.setIdentityId(EntityIdCodec.toDomain(dataObject.getIdentityId()));
        entity.setIdentityType(identityTypeFrom(dataObject.getIdentityType()));
        entity.setLoginType(dataObject.getLoginType());
        entity.setStatus(dataObject.getStatus() == null ? null : MemberAuthSessionStatus.from(dataObject.getStatus()));
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

    public static List<MemberAuthSession> toEntityList(List<MemberAuthSessionDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<MemberAuthSession> entities = new ArrayList<>();
        for (MemberAuthSessionDO dataObject : dataObjects) {
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
        if (identityType.startsWith("MEMBER_")) {
            return PrincipalIdentityType.from(identityType);
        }
        return PrincipalIdentityType.from(PrincipalType.MEMBER, identityType);
    }
}
