package com.github.thundax.modules.auth.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Getter
@Setter
@ApiModel(value = "CaptchaRefreshRequest", description = "图形验证码刷新请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaptchaRefreshRequest implements Serializable {

    @ApiModelProperty(name = "loginToken", value = "登录表单token")
    @JsonProperty("loginToken")
    @NotEmpty(message = "\"loginToken\"不能为空")
    private String loginToken;
}
