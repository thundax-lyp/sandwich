package com.github.thundax.modules.sys.api.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.vo.UserVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * AssignUserQueryParam
 *
 * @author wdit
 */
@ApiModel(value = "AssignUserQueryParam", description = "用户授权参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignUserQueryParam implements Serializable {

    private String roleId;
    private List<UserVo> users;

    @ApiModelProperty(name = "id", value = "权限ID")
    @JsonProperty("id")
    @Size(max = 64, message = "\"权限ID\"长度不能超过64")
    @NotEmpty(message = "\"权限ID\"不能为空")
    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    @ApiModelProperty(name = "users", value = "用户列表")
    @JsonProperty("users")
    public List<UserVo> getUsers() {
        return users;
    }

    public void setUsers(List<UserVo> users) {
        this.users = users;
    }
}
