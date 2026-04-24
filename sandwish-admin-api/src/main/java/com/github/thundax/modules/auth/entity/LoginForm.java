package com.github.thundax.modules.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.auth.utils.AuthUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @author wdit
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginForm implements Serializable {

    public static final int REFRESH_TOKEN_SIZE = 5;

    private static final String NULL_CAPTCHA = "null";

    /*** 登录令牌 ***/
    private String loginToken;
    /*** 刷新令牌，最多5个，按时间倒序存放 ***/
    private List<String> refreshTokenList;
    /*** 验证码 ***/
    private String captcha;
    /*** 手机号码 ***/
    private String mobile;
    /*** 短信验证码 ***/
    private String mobileValidateCode;
    /*** 超时时间 ***/
    private Integer expiredSeconds;
    /*** 校验码，由 AuthUtils.currentCheckCode 生成 ***/
    private String checkCode;

    /*** 加密传输的公钥 ***/
    private String publicKey;
    /*** 加密传输的私钥 ***/
    private String privateKey;

    public LoginForm() {
        this.captcha = NULL_CAPTCHA;
    }

    @JsonIgnore
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

    @JsonIgnore
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

    @JsonIgnore
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
