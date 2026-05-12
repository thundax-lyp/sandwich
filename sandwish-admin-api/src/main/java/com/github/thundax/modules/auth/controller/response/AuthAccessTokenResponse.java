package com.github.thundax.modules.auth.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(value = "AuthAccessTokenResponse", description = "访问令牌响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthAccessTokenResponse implements Serializable {

    @ApiModelProperty(name = "token", value = "令牌")
    @JsonProperty("token")
    private String token;

    @ApiModelProperty(name = "refreshToken", value = "刷新令牌")
    @JsonProperty("refreshToken")
    private String refreshToken;

    @ApiModelProperty(name = "expireAt", value = "访问令牌过期时间")
    @JsonProperty("expireAt")
    private Long expireAt;
}
