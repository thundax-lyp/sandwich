package com.github.thundax.modules.auth.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "AuthLoginRequest", description = "用户名密码登录请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthLoginRequest implements Serializable {

    @ApiModelProperty(name = "loginToken", value = "登录表单token")
    @JsonProperty("loginToken")
    @NotEmpty(message = "token不能为空")
    private String loginToken;

    @ApiModelProperty(name = "userName", value = "用户名")
    @JsonProperty("userName")
    @NotEmpty(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(name = "password", value = "用户密码")
    @JsonProperty("password")
    @NotEmpty(message = "用户密码不能为空")
    private String password;

    @ApiModelProperty(name = "captcha", value = "验证码")
    @JsonProperty("captcha")
    private String captcha;
}
