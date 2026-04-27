package com.github.thundax.modules.sys.response;

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
@ApiModel(value = "PersonalMenuResponse", description = "个人菜单响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalMenuResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "菜单ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "parentId", value = "父节点ID")
    @JsonProperty("parentId")
    private String parentId;

    @ApiModelProperty(name = "name", value = "名称")
    @JsonProperty("name")
    private String name;

    @ApiModelProperty(name = "priority", value = "排序数")
    @JsonProperty("priority")
    private Integer priority;

    @ApiModelProperty(name = "url", value = "URL")
    @JsonProperty("url")
    private String url;

    @ApiModelProperty(name = "displayParams", value = "显示参数，前端使用")
    @JsonProperty("displayParams")
    private String displayParams;
}
