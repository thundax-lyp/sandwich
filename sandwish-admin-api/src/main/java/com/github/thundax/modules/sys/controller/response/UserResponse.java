package com.github.thundax.modules.sys.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(value = "UserResponse", description = "用户响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "用户ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "remarks", value = "备注")
    @JsonProperty("remarks")
    private String remarks;

    @ApiModelProperty(name = "loginName", value = "登录名")
    @JsonProperty("loginName")
    private String loginName;

    @ApiModelProperty(name = "ranks", value = "等级")
    @JsonProperty("ranks")
    private Integer ranks;

    @ApiModelProperty(name = "name", value = "姓名")
    @JsonProperty("name")
    private String name;

    @ApiModelProperty(name = "email", value = "邮箱")
    @JsonProperty("email")
    private String email;

    @ApiModelProperty(name = "mobile", value = "手机号")
    @JsonProperty("mobile")
    private String mobile;

    @ApiModelProperty(name = "avatar", value = "头像链接地址")
    @JsonProperty("avatar")
    private String avatar;

    @ApiModelProperty(name = "superAdmin", value = "是否系统管理员")
    @JsonProperty("superAdmin")
    private Boolean superAdmin;

    @ApiModelProperty(name = "admin", value = "是否管理员")
    @JsonProperty("admin")
    private Boolean admin;

    @ApiModelProperty(name = "enable", value = "启用/禁用")
    @JsonProperty("enable")
    private Boolean enable;

    @ApiModelProperty(name = "department", value = "归属部门")
    @JsonProperty("department")
    private UserDepartmentResponse department;

    @ApiModelProperty(name = "roles", value = "权限")
    @JsonProperty("roles")
    private List<UserRoleResponse> roleList;
}
