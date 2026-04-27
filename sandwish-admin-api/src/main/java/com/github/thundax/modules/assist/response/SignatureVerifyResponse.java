package com.github.thundax.modules.assist.response;

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
@ApiModel(value = "SignatureVerifyResponse", description = "签名验签响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignatureVerifyResponse implements Serializable {

    @ApiModelProperty(name = "verified", value = "是否验签成功")
    @JsonProperty("verified")
    private Boolean verified;
}
