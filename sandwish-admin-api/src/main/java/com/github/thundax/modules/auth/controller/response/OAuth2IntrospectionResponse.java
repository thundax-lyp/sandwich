package com.github.thundax.modules.auth.controller.response;

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

    @ApiModelProperty(name = "sessionId", value = "会话标识")
    @JsonProperty("sessionId")
    private String sessionId;
}
