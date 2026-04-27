package com.github.thundax.modules.sys.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
@ApiModel(value = "MenuDisplayRequest", description = "菜单显示状态请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuDisplayRequest implements Serializable {

    @ApiModelProperty(name = "id", value = "菜单ID")
    @JsonProperty("id")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @ApiModelProperty(name = "display", value = "显示/隐藏")
    @JsonProperty("display")
    private Boolean display;
}
