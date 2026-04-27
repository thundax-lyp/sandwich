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
@ApiModel(value = "RoleOfficeResponse", description = "角色机构响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleOfficeResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "机构ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "name", value = "名称")
    @JsonProperty("name")
    private String name;

    @ApiModelProperty(name = "namePath", value = "全名称")
    @JsonProperty("namePath")
    private String namePath;
}
