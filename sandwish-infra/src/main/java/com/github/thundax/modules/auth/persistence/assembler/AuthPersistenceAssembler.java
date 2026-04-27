package com.github.thundax.modules.auth.persistence.assembler;

import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.persistence.dataobject.AccessTokenDO;
import com.github.thundax.modules.auth.persistence.dataobject.LoginFormDO;

/** auth Redis 持久化装配器。 */
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

    public static LoginFormDO toDataObject(LoginForm entity) {
        if (entity == null) {
            return null;
        }
        LoginFormDO dataObject = new LoginFormDO();
        dataObject.setLoginToken(entity.getLoginToken());
        dataObject.setRefreshTokenList(entity.getRefreshTokenList());
        dataObject.setCaptcha(entity.getCaptcha());
        dataObject.setMobile(entity.getMobile());
        dataObject.setMobileValidateCode(entity.getMobileValidateCode());
        dataObject.setExpiredSeconds(entity.getExpiredSeconds());
        dataObject.setCheckCode(entity.getCheckCode());
        dataObject.setPublicKey(entity.getPublicKey());
        dataObject.setPrivateKey(entity.getPrivateKey());
        return dataObject;
    }

    public static LoginForm toEntity(LoginFormDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        LoginForm entity = new LoginForm();
        entity.setLoginToken(dataObject.getLoginToken());
        entity.setRefreshTokenList(dataObject.getRefreshTokenList());
        entity.setCaptcha(dataObject.getCaptcha());
        entity.setMobile(dataObject.getMobile());
        entity.setMobileValidateCode(dataObject.getMobileValidateCode());
        entity.setExpiredSeconds(dataObject.getExpiredSeconds());
        entity.setCheckCode(dataObject.getCheckCode());
        entity.setPublicKey(dataObject.getPublicKey());
        entity.setPrivateKey(dataObject.getPrivateKey());
        return entity;
    }
}
