package com.github.thundax.modules.auth.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(value = "OAuth2AuthorizationDecisionResponse", description = "OAuth2 授权决策响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2AuthorizationDecisionResponse implements Serializable {
    @JsonProperty("approved")
    private boolean approved;

    @JsonProperty("authorizationCode")
    private String authorizationCode;

    @JsonProperty("state")
    private String state;
}
