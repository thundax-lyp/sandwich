package com.github.thundax.modules.auth.persistence.assembler;

import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.persistence.dataobject.AccessTokenDO;

public final class AuthPersistenceAssembler {

    private AuthPersistenceAssembler() {}

    public static AccessTokenDO toDataObject(AccessToken entity) {
        if (entity == null) {
            return null;
        }
        AccessTokenDO dataObject = new AccessTokenDO();
        dataObject.setToken(entity.getToken());
        dataObject.setUserId(entity.getUserId());
        dataObject.setCheckCode(entity.getCheckCode());
        return dataObject;
    }

    public static AccessToken toEntity(AccessTokenDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        AccessToken entity = new AccessToken();
        entity.setToken(dataObject.getToken());
        entity.setUserId(dataObject.getUserId());
        entity.setCheckCode(dataObject.getCheckCode());
        return entity;
    }
}
