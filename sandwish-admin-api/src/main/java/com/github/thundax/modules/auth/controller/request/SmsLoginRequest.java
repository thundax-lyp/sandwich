package com.github.thundax.modules.auth.controller.request;

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
@ApiModel(value = "SmsLoginRequest", description = "短信登录请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsLoginRequest implements Serializable {

    @ApiModelProperty(name = "loginToken", value = "登录 token")
    @JsonProperty("loginToken")
    private String loginToken;

    @ApiModelProperty(name = "mobile", value = "手机号")
    @JsonProperty("mobile")
    private String mobile;

    @ApiModelProperty(name = "validateCode", value = "短信验证码")
    @JsonProperty("validateCode")
    private String validateCode;
}
