package com.github.thundax.modules.auth.persistence.assembler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.auth.entity.OAuthAuthorization;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.persistence.dataobject.OAuthAuthorizationDO;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public final class OAuthAuthorizationPersistenceAssembler {

    private static final TypeReference<LinkedHashSet<String>> STRING_SET_TYPE =
            new TypeReference<LinkedHashSet<String>>() {};

    private OAuthAuthorizationPersistenceAssembler() {}

    public static OAuthAuthorizationDO toDataObject(OAuthAuthorization entity) {
        if (entity == null) {
            return null;
        }
        OAuthAuthorizationDO dataObject = new OAuthAuthorizationDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setAuthorizationCode(entity.getAuthorizationCode());
        dataObject.setClientId(entity.getClientId());
        dataObject.setPrincipalType(entity.getPrincipalKey().getPrincipalType().value());
        dataObject.setPrincipalId(EntityIdCodec.toValue(entity.getPrincipalKey().getPrincipalId()));
        dataObject.setRedirectUri(entity.getRedirectUri());
        dataObject.setScopes(writeStringSet(entity.getScopes()));
        dataObject.setState(entity.getState());
        dataObject.setCodeChallenge(entity.getCodeChallenge());
        dataObject.setCodeChallengeMethod(entity.getCodeChallengeMethod());
        dataObject.setIssuedAt(entity.getIssuedAt());
        dataObject.setExpireAt(entity.getExpireAt());
        dataObject.setUsed(entity.isUsed());
        return dataObject;
    }

    public static OAuthAuthorization toEntity(OAuthAuthorizationDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        OAuthAuthorization entity = new OAuthAuthorization();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setAuthorizationCode(dataObject.getAuthorizationCode());
        entity.setClientId(dataObject.getClientId());
        entity.setPrincipalKey(PrincipalKey.of(
                PrincipalType.from(dataObject.getPrincipalType()),
                EntityIdCodec.toDomain(dataObject.getPrincipalId())));
        entity.setRedirectUri(dataObject.getRedirectUri());
        entity.setScopes(readStringSet(dataObject.getScopes()));
        entity.setState(dataObject.getState());
        entity.setCodeChallenge(dataObject.getCodeChallenge());
        entity.setCodeChallengeMethod(dataObject.getCodeChallengeMethod());
        entity.setIssuedAt(dataObject.getIssuedAt());
        entity.setExpireAt(dataObject.getExpireAt());
        entity.setUsed(dataObject.isUsed());
        return entity;
    }

    public static List<OAuthAuthorization> toEntityList(List<OAuthAuthorizationDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<OAuthAuthorization> entities = new ArrayList<>();
        for (OAuthAuthorizationDO dataObject : dataObjects) {
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
}
