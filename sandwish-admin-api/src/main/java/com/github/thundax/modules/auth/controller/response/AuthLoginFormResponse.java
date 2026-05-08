package com.github.thundax.modules.auth.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

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

    @ApiModelProperty(name = "expiredAt", value = "过期时间戳，单位：毫秒。")
    @JsonProperty("expiredAt")
    private Long expiredAt;

    @ApiModelProperty(name = "publicKey", value = "公钥")
    @JsonProperty("publicKey")
    private String publicKey;
}
