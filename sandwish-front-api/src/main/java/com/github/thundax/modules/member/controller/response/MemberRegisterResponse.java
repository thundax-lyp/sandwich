package com.github.thundax.modules.member.controller.response;

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
@ApiModel(value = "MemberRegisterResponse", description = "会员注册响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberRegisterResponse implements Serializable {

    @ApiModelProperty(name = "success", value = "是否成功")
    @JsonProperty("success")
    private Boolean success;

    @ApiModelProperty(name = "memberId", value = "会员ID")
    @JsonProperty("memberId")
    private String memberId;

    @ApiModelProperty(name = "message", value = "响应消息")
    @JsonProperty("message")
    private String message;
}
