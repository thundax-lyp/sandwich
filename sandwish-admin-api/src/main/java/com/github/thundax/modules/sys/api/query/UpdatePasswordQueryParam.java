package com.github.thundax.modules.sys.api.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.modules.sys.utils.SysApiUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@ApiModel(value = "UpdatePasswordQueryParam", description = "更新密码参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePasswordQueryParam implements Serializable {

    private String oldPassword;
    private String password;
    private String token;

    @ApiModelProperty(name = "oldPassword", value = "旧密码")
    @JsonProperty("oldPassword")
    @NotEmpty(message = "\"旧密码\"不能为空")
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    @ApiModelProperty(name = "password", value = "新密码")
    @JsonProperty("password")
    @NotEmpty(message = "\"新密码\"不能为空")
    @Pattern(regexp = SysApiUtils.PASSWORD_VALIDATE_PATTERN, message = SysApiUtils.PASSWORD_VALIDATE_MESSAGE)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ApiModelProperty(name = "token", value = "令牌")
    @JsonProperty("token")
    @NotEmpty(message = "\"token\"不能为空")
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
