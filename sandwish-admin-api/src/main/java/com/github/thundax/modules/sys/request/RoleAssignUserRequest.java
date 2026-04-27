package com.github.thundax.modules.sys.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "RoleAssignUserRequest", description = "角色授权用户请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleAssignUserRequest implements Serializable {

    @ApiModelProperty(name = "id", value = "角色ID")
    @JsonProperty("id")
    @Size(max = 64, message = "\"角色ID\"长度不能超过64")
    @NotEmpty(message = "\"角色ID\"不能为空")
    private String roleId;

    @ApiModelProperty(name = "users", value = "用户列表")
    @JsonProperty("users")
    private List<RoleUserRequest> users;
}
