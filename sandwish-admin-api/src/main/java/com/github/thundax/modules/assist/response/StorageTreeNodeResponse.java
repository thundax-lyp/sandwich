package com.github.thundax.modules.assist.response;

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
@ApiModel(value = "StorageTreeNodeResponse", description = "存储树节点响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageTreeNodeResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "节点ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "pId", value = "父节点ID")
    @JsonProperty("pId")
    private String parentId;

    @ApiModelProperty(name = "name", value = "节点名称")
    @JsonProperty("name")
    private String name;
}
