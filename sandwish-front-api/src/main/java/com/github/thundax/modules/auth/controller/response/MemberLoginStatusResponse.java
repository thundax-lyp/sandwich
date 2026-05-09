package com.github.thundax.modules.auth.controller.response;

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
@ApiModel(value = "MemberLoginStatusResponse", description = "会员登录状态响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberLoginStatusResponse implements Serializable {

    @ApiModelProperty(name = "loggedIn", value = "是否已登录")
    @JsonProperty("loggedIn")
    private Boolean loggedIn;

    @ApiModelProperty(name = "memberId", value = "当前会员ID")
    @JsonProperty("memberId")
    private String memberId;

    @ApiModelProperty(name = "message", value = "响应消息")
    @JsonProperty("message")
    private String message;
}
