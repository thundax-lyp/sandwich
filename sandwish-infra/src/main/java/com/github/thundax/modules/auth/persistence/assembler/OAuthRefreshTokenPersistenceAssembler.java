package com.github.thundax.modules.auth.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.entity.OAuthRefreshToken;
import com.github.thundax.modules.auth.entity.enums.OAuthRefreshTokenStatus;
import com.github.thundax.modules.auth.persistence.dataobject.OAuthRefreshTokenDO;
import java.util.ArrayList;
import java.util.List;

public final class OAuthRefreshTokenPersistenceAssembler {

    private OAuthRefreshTokenPersistenceAssembler() {}

    public static OAuthRefreshTokenDO toDataObject(OAuthRefreshToken entity) {
        if (entity == null) {
            return null;
        }
        OAuthRefreshTokenDO dataObject = new OAuthRefreshTokenDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setTokenId(entity.getTokenId());
        dataObject.setTokenHash(entity.getTokenHash());
        dataObject.setAccessTokenId(entity.getAccessTokenId());
        dataObject.setClientId(entity.getClientId());
        dataObject.setTenantId(entity.getTenantId());
        dataObject.setUserId(EntityIdCodec.toValue(entity.getUserId()));
        dataObject.setIssuedAt(entity.getIssuedAt());
        dataObject.setExpireAt(entity.getExpireAt());
        dataObject.setStatus(statusValue(entity.getStatus()));
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateBy(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateBy(entity.getUpdateUserId());
        return dataObject;
    }

    public static OAuthRefreshToken toEntity(OAuthRefreshTokenDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        OAuthRefreshToken entity = new OAuthRefreshToken();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setTokenId(dataObject.getTokenId());
        entity.setTokenHash(dataObject.getTokenHash());
        entity.setAccessTokenId(dataObject.getAccessTokenId());
        entity.setClientId(dataObject.getClientId());
        entity.setTenantId(dataObject.getTenantId());
        entity.setUserId(EntityIdCodec.toDomain(dataObject.getUserId()));
        entity.setIssuedAt(dataObject.getIssuedAt());
        entity.setExpireAt(dataObject.getExpireAt());
        entity.setStatus(statusFrom(dataObject.getStatus()));
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateBy());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateBy());
        return entity;
    }

    public static List<OAuthRefreshToken> toEntityList(List<OAuthRefreshTokenDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<OAuthRefreshToken> entities = new ArrayList<>();
        for (OAuthRefreshTokenDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static String statusValue(OAuthRefreshTokenStatus status) {
        return status == null ? null : status.value();
    }

    private static OAuthRefreshTokenStatus statusFrom(String status) {
        return status == null ? null : OAuthRefreshTokenStatus.from(status);
    }
}
