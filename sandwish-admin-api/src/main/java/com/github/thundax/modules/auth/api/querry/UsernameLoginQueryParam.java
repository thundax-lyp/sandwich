package com.github.thundax.modules.auth.api.querry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.NotEmpty;

/**
 * UsernameLoginQueryParam
 *
 * @author wdit
 */
@ApiModel(value = "UsernameLoginQueryParam", description = "用户名/密码登录参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsernameLoginQueryParam implements Serializable {

    private String loginToken;
    private String username;
    private String password;
    private String captcha;

    @ApiModelProperty(name = "loginToken", value = "登录表单token")
    @JsonProperty("loginToken")
    @NotEmpty(message = "token不能为空")
    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    @ApiModelProperty(name = "userName", value = "用户名")
    @JsonProperty("userName")
    @NotEmpty(message = "用户名不能为空")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @ApiModelProperty(name = "password", value = "用户密码")
    @JsonProperty("password")
    @NotEmpty(message = "用户密码不能为空")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ApiModelProperty(name = "captcha", value = "验证码")
    @JsonProperty("captcha")
    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
}
