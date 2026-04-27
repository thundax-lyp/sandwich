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
@ApiModel(value = "MenuQueryRequest", description = "菜单查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuQueryRequest implements Serializable {

    @ApiModelProperty(name = "parentId", value = "父节点ID，为\"ROOT\"则查询跟节点")
    @JsonProperty("parentId")
    @Size(max = 64, message = "\"父节点ID\"长度不能超过 64")
    private String parentId;

    @ApiModelProperty(name = "display", value = "显示/隐藏")
    @JsonProperty("display")
    private Boolean display;
}
