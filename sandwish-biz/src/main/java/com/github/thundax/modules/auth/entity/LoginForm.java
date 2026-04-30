package com.github.thundax.modules.auth.entity;

import com.github.thundax.modules.auth.utils.AuthUtils;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class LoginForm implements Serializable {

    public static final int REFRESH_TOKEN_SIZE = 5;

    private static final String NULL_CAPTCHA = "null";

    private String loginToken;

    private List<String> refreshTokenList;

    private String captcha;

    private String mobile;

    private String mobileValidateCode;

    private Integer expiredSeconds;

    private String checkCode;

    private String publicKey;

    private String privateKey;

    public LoginForm() {
        this.captcha = NULL_CAPTCHA;
    }

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

    public boolean isNullCaptcha() {
        return isNullCaptcha(captcha);
    }

    public static boolean isNullCaptcha(String captcha) {
        return StringUtils.equals(NULL_CAPTCHA, captcha);
    }

    public boolean validateCheckCode() {
        return AuthUtils.validateCheckCode(getCheckCode());
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
