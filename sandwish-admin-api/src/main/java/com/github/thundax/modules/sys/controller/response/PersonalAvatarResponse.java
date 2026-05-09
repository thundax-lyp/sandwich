package com.github.thundax.modules.sys.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@ApiModel(value = "PersonalAvatarResponse", description = "当前用户头像响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalAvatarResponse implements Serializable {

    @ApiModelProperty(name = "avatar", value = "头像链接地址")
    @JsonProperty("avatar")
    private String avatar;
}
