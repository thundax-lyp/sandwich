package com.github.thundax.modules.auth.service.dto;

import java.util.List;

public class PreAuthSessionDTO {

    private String loginToken;

    private List<String> refreshTokenList;

    private Integer expiredSeconds;

    private String publicKey;

    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    public List<String> getRefreshTokenList() {
        return refreshTokenList;
    }

    public void setRefreshTokenList(List<String> refreshTokenList) {
        this.refreshTokenList = refreshTokenList;
    }

    public Integer getExpiredSeconds() {
        return expiredSeconds;
    }

    public void setExpiredSeconds(Integer expiredSeconds) {
        this.expiredSeconds = expiredSeconds;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
