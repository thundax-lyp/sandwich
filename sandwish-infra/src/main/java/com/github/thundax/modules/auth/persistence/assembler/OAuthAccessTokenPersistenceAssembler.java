package com.github.thundax.modules.auth.persistence.assembler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.auth.entity.OAuthAccessToken;
import com.github.thundax.modules.auth.entity.enums.OAuthAccessTokenStatus;
import com.github.thundax.modules.auth.persistence.dataobject.OAuthAccessTokenDO;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public final class OAuthAccessTokenPersistenceAssembler {

    private static final TypeReference<LinkedHashSet<String>> STRING_SET_TYPE =
            new TypeReference<LinkedHashSet<String>>() {};

    private OAuthAccessTokenPersistenceAssembler() {}

    public static OAuthAccessTokenDO toDataObject(OAuthAccessToken entity) {
        if (entity == null) {
            return null;
        }
        OAuthAccessTokenDO dataObject = new OAuthAccessTokenDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setTokenId(entity.getTokenId());
        dataObject.setTokenHash(entity.getTokenHash());
        dataObject.setClientId(entity.getClientId());
        dataObject.setTenantId(entity.getTenantId());
        dataObject.setUserId(EntityIdCodec.toValue(entity.getUserId()));
        dataObject.setScopes(writeStringSet(entity.getScopes()));
        dataObject.setIssuedAt(entity.getIssuedAt());
        dataObject.setExpireAt(entity.getExpireAt());
        dataObject.setStatus(statusValue(entity.getStatus()));
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateBy(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateBy(entity.getUpdateUserId());
        return dataObject;
    }

    public static OAuthAccessToken toEntity(OAuthAccessTokenDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        OAuthAccessToken entity = new OAuthAccessToken();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setTokenId(dataObject.getTokenId());
        entity.setTokenHash(dataObject.getTokenHash());
        entity.setClientId(dataObject.getClientId());
        entity.setTenantId(dataObject.getTenantId());
        entity.setUserId(EntityIdCodec.toDomain(dataObject.getUserId()));
        entity.setScopes(readStringSet(dataObject.getScopes()));
        entity.setIssuedAt(dataObject.getIssuedAt());
        entity.setExpireAt(dataObject.getExpireAt());
        entity.setStatus(statusFrom(dataObject.getStatus()));
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateBy());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateBy());
        return entity;
    }

    public static List<OAuthAccessToken> toEntityList(List<OAuthAccessTokenDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<OAuthAccessToken> entities = new ArrayList<>();
        for (OAuthAccessTokenDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static String writeStringSet(Set<String> values) {
        return values == null ? "[]" : JsonUtils.toJson(values);
    }

    private static LinkedHashSet<String> readStringSet(String value) {
        if (StringUtils.isBlank(value)) {
            return new LinkedHashSet<>();
        }
        LinkedHashSet<String> values = JsonUtils.fromJson(value, STRING_SET_TYPE);
        return values == null ? new LinkedHashSet<>() : values;
    }

    private static String statusValue(OAuthAccessTokenStatus status) {
        return status == null ? null : status.value();
    }

    private static OAuthAccessTokenStatus statusFrom(String status) {
        return status == null ? null : OAuthAccessTokenStatus.from(status);
    }
}
