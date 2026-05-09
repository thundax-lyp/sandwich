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
@ApiModel(value = "OAuth2IntrospectionResponse", description = "OAuth2 token introspection 响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2IntrospectionResponse implements Serializable {

    @ApiModelProperty(name = "active", value = "是否活跃")
    @JsonProperty("active")
    private boolean active;

    @ApiModelProperty(name = "sub", value = "用户标识")
    @JsonProperty("sub")
    private String subject;

    @ApiModelProperty(name = "username", value = "用户名")
    @JsonProperty("username")
    private String username;

    @ApiModelProperty(name = "client_id", value = "OAuth2 客户端标识")
    @JsonProperty("client_id")
    private String clientId;

    @ApiModelProperty(name = "scope", value = "OAuth2 授权范围")
    @JsonProperty("scope")
    private String scope;

    @ApiModelProperty(name = "exp", value = "OAuth2 token 过期时间戳")
    @JsonProperty("exp")
    private Long expiresAt;

    @ApiModelProperty(name = "token_type", value = "OAuth2 token 类型")
    @JsonProperty("token_type")
    private String tokenType;

    @ApiModelProperty(name = "sessionId", value = "会话标识")
    @JsonProperty("sessionId")
    private String sessionId;
}
