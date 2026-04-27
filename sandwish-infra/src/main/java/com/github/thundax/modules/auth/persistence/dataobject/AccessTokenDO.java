package com.github.thundax.modules.auth.persistence.dataobject;

import java.io.Serializable;

/** access token DO。 */
public class AccessTokenDO implements Serializable {

    private String token;
    private String userId;
    private String checkCode;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }
}
