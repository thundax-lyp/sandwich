package com.github.thundax.modules.member.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.member.entity.MemberAccessToken;
import com.github.thundax.modules.member.entity.enums.MemberAccessTokenStatus;
import com.github.thundax.modules.member.persistence.dataobject.MemberAccessTokenDO;
import java.util.ArrayList;
import java.util.List;

public final class MemberAccessTokenPersistenceAssembler {
    private MemberAccessTokenPersistenceAssembler() {}

    public static MemberAccessTokenDO toDataObject(MemberAccessToken entity) {
        if (entity == null) {
            return null;
        }
        MemberAccessTokenDO dataObject = new MemberAccessTokenDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setTokenId(entity.getTokenId());
        dataObject.setTokenHash(entity.getTokenHash());
        dataObject.setSessionId(entity.getSessionId());
        dataObject.setMemberId(EntityIdCodec.toValue(entity.getMemberId()));
        dataObject.setIssuedAt(entity.getIssuedAt());
        dataObject.setExpireAt(entity.getExpireAt());
        dataObject.setStatus(
                entity.getStatus() == null ? null : entity.getStatus().value());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateBy(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateBy(entity.getUpdateUserId());
        return dataObject;
    }

    public static MemberAccessToken toEntity(MemberAccessTokenDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        MemberAccessToken entity = new MemberAccessToken();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setTokenId(dataObject.getTokenId());
        entity.setTokenHash(dataObject.getTokenHash());
        entity.setSessionId(dataObject.getSessionId());
        entity.setMemberId(EntityIdCodec.toDomain(dataObject.getMemberId()));
        entity.setIssuedAt(dataObject.getIssuedAt());
        entity.setExpireAt(dataObject.getExpireAt());
        entity.setStatus(dataObject.getStatus() == null ? null : MemberAccessTokenStatus.from(dataObject.getStatus()));
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateBy());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateBy());
        return entity;
    }

    public static List<MemberAccessToken> toEntityList(List<MemberAccessTokenDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<MemberAccessToken> entities = new ArrayList<>();
        for (MemberAccessTokenDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }
}
