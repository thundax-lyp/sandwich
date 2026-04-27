package com.github.thundax.modules.sys.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.modules.sys.utils.SysApiUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Getter
@Setter
@ApiModel(value = "PersonalPasswordUpdateRequest", description = "个人密码更新请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalPasswordUpdateRequest implements Serializable {

    @ApiModelProperty(name = "oldPassword", value = "旧密码")
    @JsonProperty("oldPassword")
    @NotEmpty(message = "\"旧密码\"不能为空")
    private String oldPassword;

    @ApiModelProperty(name = "password", value = "新密码")
    @JsonProperty("password")
    @NotEmpty(message = "\"新密码\"不能为空")
    @Pattern(regexp = SysApiUtils.PASSWORD_VALIDATE_PATTERN, message = SysApiUtils.PASSWORD_VALIDATE_MESSAGE)
    private String password;

    @ApiModelProperty(name = "token", value = "令牌")
    @JsonProperty("token")
    @NotEmpty(message = "\"token\"不能为空")
    private String token;
}
