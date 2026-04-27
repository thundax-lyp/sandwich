package com.github.thundax.modules.auth.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@ApiModel(value = "CaptchaRefreshResponse", description = "图形验证码刷新响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaptchaRefreshResponse implements Serializable {

    @ApiModelProperty(name = "refreshed", value = "是否已刷新")
    @JsonProperty("refreshed")
    private Boolean refreshed;
}
