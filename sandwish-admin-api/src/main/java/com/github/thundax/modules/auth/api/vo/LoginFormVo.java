package com.github.thundax.modules.auth.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.UUID;

@ApiModel(value = "LoginForm", description = "登录表单")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginFormVo implements Serializable {

    private String loginToken;
    private String refreshToken;
    private Integer expireSeconds;

    private String publicKey;

    public LoginFormVo() {
        this.loginToken = UUID.randomUUID().toString();
    }

    public LoginFormVo(String loginToken, Integer expireSeconds) {
        this.loginToken = loginToken;
        this.expireSeconds = expireSeconds;
    }

    @ApiModelProperty(name = "loginToken", value = "登录令牌")
    @JsonProperty("loginToken")
    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    @ApiModelProperty(name = "refreshToken", value = "刷新令牌")
    @JsonProperty("refreshToken")
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @ApiModelProperty(name = "expireSeconds", value = "超时时间，单位：秒。")
    @JsonProperty("expireSeconds")
    public Integer getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(Integer expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    @ApiModelProperty(name = "publicKey", value = "公钥")
    @JsonProperty("publicKey")
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
