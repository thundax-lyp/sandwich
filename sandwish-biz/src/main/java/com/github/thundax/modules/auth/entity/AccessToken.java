package com.github.thundax.modules.auth.entity;

import java.io.Serializable;

public class AccessToken implements Serializable {

    public static final int REFRESH_TOKEN_SIZE = 5;

    private String token;

    private String userId;

    private String checkCode;

    public AccessToken() {}

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
