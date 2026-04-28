package com.github.thundax.modules.auth.persistence.dataobject;

import java.io.Serializable;
import java.util.List;

public class LoginFormDO implements Serializable {

    private String loginToken;
    private List<String> refreshTokenList;
    private String captcha;
    private String mobile;
    private String mobileValidateCode;
    private Integer expiredSeconds;
    private String checkCode;
    private String publicKey;
    private String privateKey;

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

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobileValidateCode() {
        return mobileValidateCode;
    }

    public void setMobileValidateCode(String mobileValidateCode) {
        this.mobileValidateCode = mobileValidateCode;
    }

    public Integer getExpiredSeconds() {
        return expiredSeconds;
    }

    public void setExpiredSeconds(Integer expiredSeconds) {
        this.expiredSeconds = expiredSeconds;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
