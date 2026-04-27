package com.github.thundax.modules.sys.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "RoleSaveRequest", description = "角色保存请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleSaveRequest implements Serializable {

    @ApiModelProperty(name = "id", value = "角色ID")
    @JsonProperty("id")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @ApiModelProperty(name = "priority", value = "排序数", example = "0")
    @JsonProperty("priority")
    @Min(value = 0, message = "\"排序数\"必须不能小于 0")
    private Integer priority;

    @ApiModelProperty(name = "remarks", value = "备注")
    @JsonProperty("remarks")
    @Size(max = 200, message = "\"备注\"长度不能超过 200")
    private String remarks;

    @ApiModelProperty(name = "name", value = "名称")
    @JsonProperty("name")
    @NotEmpty(message = "\"名称\"不能为空")
    @Size(max = 50, message = "\"名称\"长度不能超过 50")
    private String name;

    @ApiModelProperty(name = "admin", value = "是否管理权限")
    @JsonProperty("admin")
    private Boolean admin;

    @ApiModelProperty(name = "enable", value = "启用/禁用")
    @JsonProperty("enable")
    private Boolean enable;

    @ApiModelProperty(name = "menus", value = "菜单列表")
    @JsonProperty("menus")
    private List<RoleMenuRequest> menuList;
}
