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
@ApiModel(value = "RoleUserTreeNodeResponse", description = "角色用户树节点响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleUserTreeNodeResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "节点ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "parentId", value = "父节点ID")
    @JsonProperty("parentId")
    private String parentId;

    @ApiModelProperty(name = "name", value = "名称")
    @JsonProperty("name")
    private String name;

    @ApiModelProperty(name = "user", value = "用户，为空则是部门")
    @JsonProperty("user")
    private RoleUserResponse user;
}
