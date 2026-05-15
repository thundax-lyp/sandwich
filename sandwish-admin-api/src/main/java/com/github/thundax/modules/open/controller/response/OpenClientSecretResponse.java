package com.github.thundax.modules.open.controller.response;

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
@ApiModel(value = "OpenClientSecretResponse", description = "开放客户端密钥响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenClientSecretResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "开放客户端ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "apiKey", value = "API KEY")
    @JsonProperty("apiKey")
    private String apiKey;

    @ApiModelProperty(name = "apiSecret", value = "API SECRET，仅创建或重置时返回一次")
    @JsonProperty("apiSecret")
    private String apiSecret;
}
