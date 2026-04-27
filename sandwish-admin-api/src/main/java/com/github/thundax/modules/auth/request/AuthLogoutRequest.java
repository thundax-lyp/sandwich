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
@ApiModel(value = "AuthLogoutRequest", description = "退出登录请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthLogoutRequest implements Serializable {

    @ApiModelProperty(name = "token", value = "令牌")
    @JsonProperty("token")
    @NotEmpty(message = "\"token\"不能为空")
    private String token;
}
