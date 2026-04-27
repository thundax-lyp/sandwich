package com.github.thundax.modules.auth.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@ApiModel(value = "AuthLoginFormResponse", description = "登录表单响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthLoginFormResponse implements Serializable {

    @ApiModelProperty(name = "loginToken", value = "登录令牌")
    @JsonProperty("loginToken")
    private String loginToken;

    @ApiModelProperty(name = "refreshToken", value = "刷新令牌")
    @JsonProperty("refreshToken")
    private String refreshToken;

    @ApiModelProperty(name = "expireSeconds", value = "超时时间，单位：秒。")
    @JsonProperty("expireSeconds")
    private Integer expireSeconds;

    @ApiModelProperty(name = "publicKey", value = "公钥")
    @JsonProperty("publicKey")
    private String publicKey;
}
