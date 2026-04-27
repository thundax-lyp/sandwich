package com.github.thundax.modules.assist.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@ApiModel(value = "KeypairPublicKeyResponse", description = "公钥响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeypairPublicKeyResponse implements Serializable {

    @ApiModelProperty(name = "publicKey", value = "公钥")
    @JsonProperty("publicKey")
    private String publicKey;
}
