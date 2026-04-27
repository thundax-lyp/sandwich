package com.github.thundax.modules.assist.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Getter
@Setter
@ApiModel(value = "KeypairPublicKeyRequest", description = "公钥获取请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeypairPublicKeyRequest implements Serializable {

    @ApiModelProperty(name = "token", value = "令牌")
    @JsonProperty("token")
    @NotEmpty(message = "\"token\"不能为空")
    private String token;
}
