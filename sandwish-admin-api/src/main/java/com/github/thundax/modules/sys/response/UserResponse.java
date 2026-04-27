package com.github.thundax.modules.sys.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "UserResponse", description = "用户响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "用户ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "priority", value = "排序数")
    @JsonProperty("priority")
    private Integer priority;

    @ApiModelProperty(name = "remarks", value = "备注")
    @JsonProperty("remarks")
    private String remarks;

    @ApiModelProperty(name = "createDate", value = "创建时间")
    @JsonProperty("createDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

    @ApiModelProperty(name = "updateDate", value = "修改时间")
    @JsonProperty("updateDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateDate;

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

    @ApiModelProperty(name = "registerDate", value = "注册时间")
    @JsonProperty("registerDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date registerDate;

    @ApiModelProperty(name = "registerIp", value = "注册IP")
    @JsonProperty("registerIp")
    private String registerIp;

    @ApiModelProperty(name = "lastLoginDate", value = "最后登录时间")
    @JsonProperty("lastLoginDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginDate;

    @ApiModelProperty(name = "lastLoginIp", value = "最后登录IP")
    @JsonProperty("lastLoginIp")
    private String lastLoginIp;

    @ApiModelProperty(name = "office", value = "归属组织机构")
    @JsonProperty("office")
    private UserOfficeResponse office;

    @ApiModelProperty(name = "roles", value = "权限")
    @JsonProperty("roles")
    private List<UserRoleResponse> roleList;
}
