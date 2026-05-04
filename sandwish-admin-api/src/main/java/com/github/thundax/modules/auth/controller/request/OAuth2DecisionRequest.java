package com.github.thundax.modules.auth.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "OAuth2DecisionRequest", description = "OAuth2 授权决策请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2DecisionRequest implements Serializable {
    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("redirectUri")
    private String redirectUri;

    @JsonProperty("scopes")
    private List<String> scopes;

    @JsonProperty("state")
    private String state;

    @JsonProperty("codeChallenge")
    private String codeChallenge;

    @JsonProperty("codeChallengeMethod")
    private String codeChallengeMethod;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("approved")
    private boolean approved;
}
