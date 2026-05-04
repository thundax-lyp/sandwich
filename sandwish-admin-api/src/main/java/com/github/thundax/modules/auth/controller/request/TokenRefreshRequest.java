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
@ApiModel(value = "TokenRefreshRequest", description = "Token 刷新请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenRefreshRequest implements Serializable {

    @ApiModelProperty(name = "clientId", value = "客户端标识")
    @JsonProperty("clientId")
    private String clientId;

    @ApiModelProperty(name = "refreshToken", value = "refresh token")
    @JsonProperty("refreshToken")
    private String refreshToken;
}
