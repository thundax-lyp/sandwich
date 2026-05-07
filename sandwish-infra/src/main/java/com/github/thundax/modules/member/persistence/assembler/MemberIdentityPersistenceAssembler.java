package com.github.thundax.modules.member.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.member.entity.MemberIdentity;
import com.github.thundax.modules.member.entity.enums.MemberIdentityStatus;
import com.github.thundax.modules.member.entity.enums.MemberIdentityType;
import com.github.thundax.modules.member.persistence.dataobject.MemberIdentityDO;
import java.util.ArrayList;
import java.util.List;

public final class MemberIdentityPersistenceAssembler {

    private MemberIdentityPersistenceAssembler() {}

    public static MemberIdentityDO toDataObject(MemberIdentity entity) {
        if (entity == null) {
            return null;
        }
        MemberIdentityDO dataObject = new MemberIdentityDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setMemberId(EntityIdCodec.toValue(entity.getMemberId()));
        dataObject.setIdentityType(identityTypeValue(entity.getIdentityType()));
        dataObject.setIdentityValue(entity.getIdentityValue());
        dataObject.setStatus(statusValue(entity.getStatus()));
        return dataObject;
    }

    public static MemberIdentity toEntity(MemberIdentityDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        MemberIdentity entity = new MemberIdentity();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setMemberId(EntityIdCodec.toDomain(dataObject.getMemberId()));
        entity.setIdentityType(identityTypeFrom(dataObject.getIdentityType()));
        entity.setIdentityValue(dataObject.getIdentityValue());
        entity.setStatus(statusFrom(dataObject.getStatus()));
        return entity;
    }

    public static List<MemberIdentity> toEntityList(List<MemberIdentityDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<MemberIdentity> entities = new ArrayList<>();
        for (MemberIdentityDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static String identityTypeValue(MemberIdentityType identityType) {
        return identityType == null ? null : identityType.value();
    }

    private static MemberIdentityType identityTypeFrom(String identityType) {
        return identityType == null ? null : MemberIdentityType.from(identityType);
    }

    private static String statusValue(MemberIdentityStatus status) {
        return status == null ? null : status.value();
    }

    private static MemberIdentityStatus statusFrom(String status) {
        return status == null ? null : MemberIdentityStatus.from(status);
    }
}
