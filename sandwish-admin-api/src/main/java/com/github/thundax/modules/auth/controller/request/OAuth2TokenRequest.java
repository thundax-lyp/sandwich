package com.github.thundax.modules.auth.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "OAuth2TokenRequest", description = "OAuth2 token 请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2TokenRequest implements Serializable {
    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("authorizationCode")
    private String authorizationCode;
}
