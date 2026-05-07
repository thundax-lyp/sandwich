package com.github.thundax.modules.auth.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.entity.MemberRefreshToken;
import com.github.thundax.modules.auth.entity.enums.MemberRefreshTokenStatus;
import com.github.thundax.modules.auth.persistence.dataobject.MemberRefreshTokenDO;
import java.util.ArrayList;
import java.util.List;

public final class MemberRefreshTokenPersistenceAssembler {
    private MemberRefreshTokenPersistenceAssembler() {}

    public static MemberRefreshTokenDO toDataObject(MemberRefreshToken entity) {
        if (entity == null) {
            return null;
        }
        MemberRefreshTokenDO dataObject = new MemberRefreshTokenDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setTokenId(entity.getTokenId());
        dataObject.setTokenHash(entity.getTokenHash());
        dataObject.setAccessTokenId(entity.getAccessTokenId());
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

    public static MemberRefreshToken toEntity(MemberRefreshTokenDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        MemberRefreshToken entity = new MemberRefreshToken();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setTokenId(dataObject.getTokenId());
        entity.setTokenHash(dataObject.getTokenHash());
        entity.setAccessTokenId(dataObject.getAccessTokenId());
        entity.setSessionId(dataObject.getSessionId());
        entity.setMemberId(EntityIdCodec.toDomain(dataObject.getMemberId()));
        entity.setIssuedAt(dataObject.getIssuedAt());
        entity.setExpireAt(dataObject.getExpireAt());
        entity.setStatus(dataObject.getStatus() == null ? null : MemberRefreshTokenStatus.from(dataObject.getStatus()));
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateBy());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateBy());
        return entity;
    }

    public static List<MemberRefreshToken> toEntityList(List<MemberRefreshTokenDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<MemberRefreshToken> entities = new ArrayList<>();
        for (MemberRefreshTokenDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }
}
