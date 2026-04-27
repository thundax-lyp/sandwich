package com.github.thundax.modules.sys.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.modules.sys.utils.SysApiUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "UserSaveRequest", description = "用户保存请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSaveRequest implements Serializable {

    @ApiModelProperty(name = "id", value = "用户ID")
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

    @ApiModelProperty(name = "loginName", value = "登录名")
    @JsonProperty("loginName")
    @NotEmpty(message = "\"登录名\"不能为空")
    @Size(max = 30, message = "\"登录名\"长度不能超过 30")
    private String loginName;

    @ApiModelProperty(name = "loginPass", value = "登录密码")
    @JsonProperty("loginPass")
    @Pattern(regexp = SysApiUtils.PASSWORD_VALIDATE_PATTERN, message = SysApiUtils.PASSWORD_VALIDATE_MESSAGE)
    private String loginPass;

    @ApiModelProperty(name = "ranks", value = "等级")
    @JsonProperty("ranks")
    @Min(value = 0, message = "等级不能小于0")
    @Max(value = 9, message = "等级不能超过9")
    private Integer ranks;

    @ApiModelProperty(name = "name", value = "姓名")
    @JsonProperty("name")
    @NotEmpty(message = "\"姓名\"不能为空")
    @Size(max = 50, message = "\"姓名\"长度不能超过 50")
    private String name;

    @ApiModelProperty(name = "email", value = "邮箱")
    @JsonProperty("email")
    @Size(max = 50, message = "\"邮箱\"长度不能超过 50")
    private String email;

    @ApiModelProperty(name = "mobile", value = "手机号")
    @JsonProperty("mobile")
    @Size(max = 30, message = "\"手机号\"长度不能超过 30")
    private String mobile;

    @ApiModelProperty(name = "admin", value = "是否管理员")
    @JsonProperty("admin")
    private Boolean admin;

    @ApiModelProperty(name = "enable", value = "启用/禁用")
    @JsonProperty("enable")
    private Boolean enable;

    @ApiModelProperty(name = "ssoLoginName", value = "sso登录名")
    @JsonProperty("ssoLoginName")
    private String ssoLoginName;

    @ApiModelProperty(name = "token", value = "令牌")
    @JsonProperty("token")
    @NotEmpty(message = "\"token\"不能为空")
    private String token;

    @ApiModelProperty(name = "office", value = "归属组织机构")
    @JsonProperty("office")
    private UserOfficeRequest office;

    @ApiModelProperty(name = "roles", value = "权限")
    @JsonProperty("roles")
    private List<UserRoleRequest> roleList;
}
