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
@ApiModel(value = "UserOfficeResponse", description = "用户机构响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserOfficeResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "机构ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "parentId", value = "父节点ID")
    @JsonProperty("parentId")
    private String parentId;

    @ApiModelProperty(name = "name", value = "名称")
    @JsonProperty("name")
    private String name;

    @ApiModelProperty(name = "shortName", value = "简称")
    @JsonProperty("shortName")
    private String shortName;

    @ApiModelProperty(name = "namePath", value = "全名称")
    @JsonProperty("namePath")
    private String namePath;
}
