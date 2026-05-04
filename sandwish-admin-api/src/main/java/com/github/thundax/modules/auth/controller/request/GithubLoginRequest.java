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
@ApiModel(value = "GithubLoginRequest", description = "GitHub 登录请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubLoginRequest implements Serializable {

    @ApiModelProperty(name = "code", value = "GitHub 授权码")
    @JsonProperty("code")
    private String code;
}
