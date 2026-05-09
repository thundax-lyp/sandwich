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
@ApiModel(value = "OAuth2UserinfoResponse", description = "OAuth2 userinfo 响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2UserinfoResponse implements Serializable {

    @ApiModelProperty(name = "sub", value = "用户标识")
    @JsonProperty("sub")
    private String subject;

    @ApiModelProperty(name = "username", value = "用户名")
    @JsonProperty("username")
    private String username;

    @ApiModelProperty(name = "preferred_username", value = "首选用户名")
    @JsonProperty("preferred_username")
    private String preferredUsername;

    @ApiModelProperty(name = "name", value = "用户名称")
    @JsonProperty("name")
    private String name;
}
