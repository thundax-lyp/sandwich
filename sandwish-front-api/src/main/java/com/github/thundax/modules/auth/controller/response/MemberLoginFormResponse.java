package com.github.thundax.modules.auth.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(value = "MemberLoginFormResponse", description = "会员登录表单响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberLoginFormResponse implements Serializable {
    private String loginToken;
    private String refreshToken;
    private String captcha;
    private Long expiredAt;
    private String checkCode;
    private String publicKey;
}
