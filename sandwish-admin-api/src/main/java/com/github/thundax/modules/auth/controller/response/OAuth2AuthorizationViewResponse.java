package com.github.thundax.modules.auth.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "OAuth2AuthorizationViewResponse", description = "OAuth2 授权视图响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2AuthorizationViewResponse implements Serializable {
    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("clientName")
    private String clientName;

    @JsonProperty("redirectUri")
    private String redirectUri;

    @JsonProperty("scopes")
    private Set<String> scopes;

    @JsonProperty("state")
    private String state;
}
