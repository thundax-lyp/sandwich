package com.github.thundax.modules.member.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.member.entity.MemberCredential;
import com.github.thundax.modules.member.entity.enums.MemberCredentialStatus;
import com.github.thundax.modules.member.entity.enums.MemberCredentialType;
import com.github.thundax.modules.member.persistence.dataobject.MemberCredentialDO;
import java.util.ArrayList;
import java.util.List;

public final class MemberCredentialPersistenceAssembler {

    private MemberCredentialPersistenceAssembler() {}

    public static MemberCredentialDO toDataObject(MemberCredential entity) {
        if (entity == null) {
            return null;
        }
        MemberCredentialDO dataObject = new MemberCredentialDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setMemberId(EntityIdCodec.toValue(entity.getMemberId()));
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
        return dataObject;
    }

    public static MemberCredential toEntity(MemberCredentialDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        MemberCredential entity = new MemberCredential();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setMemberId(EntityIdCodec.toDomain(dataObject.getMemberId()));
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
        return entity;
    }

    public static List<MemberCredential> toEntityList(List<MemberCredentialDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<MemberCredential> entities = new ArrayList<>();
        for (MemberCredentialDO dataObject : dataObjects) {
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

    private static String credentialTypeValue(MemberCredentialType credentialType) {
        return credentialType == null ? null : credentialType.value();
    }

    private static MemberCredentialType credentialTypeFrom(String credentialType) {
        return credentialType == null ? null : MemberCredentialType.from(credentialType);
    }

    private static String statusValue(MemberCredentialStatus status) {
        return status == null ? null : status.value();
    }

    private static MemberCredentialStatus statusFrom(String status) {
        return status == null ? null : MemberCredentialStatus.from(status);
    }
}
