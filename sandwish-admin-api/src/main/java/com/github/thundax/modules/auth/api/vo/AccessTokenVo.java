package com.github.thundax.modules.auth.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

/** @author wdit */
@ApiModel(value = "Token", description = "会话令牌")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessTokenVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    public AccessTokenVo() {}

    @ApiModelProperty(name = "token", value = "令牌")
    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
