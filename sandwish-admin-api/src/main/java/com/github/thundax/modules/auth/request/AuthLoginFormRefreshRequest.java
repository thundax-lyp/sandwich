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
@ApiModel(value = "AuthLoginFormRefreshRequest", description = "登录表单刷新请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthLoginFormRefreshRequest implements Serializable {

    @ApiModelProperty(name = "refreshToken", value = "刷新令牌")
    @JsonProperty("refreshToken")
    @NotEmpty(message = "\"refreshToken\"不能为空")
    private String refreshToken;
}
