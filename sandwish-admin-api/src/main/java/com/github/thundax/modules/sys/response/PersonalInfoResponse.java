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

@Getter
@Setter
@ApiModel(value = "PersonalInfoResponse", description = "个人信息响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalInfoResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "用户ID")
    @JsonProperty("id")
    private String id;

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

    @ApiModelProperty(name = "lastLoginDate", value = "最后登录时间")
    @JsonProperty("lastLoginDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginDate;

    @ApiModelProperty(name = "lastLoginIp", value = "最后登录IP")
    @JsonProperty("lastLoginIp")
    private String lastLoginIp;

    @ApiModelProperty(name = "registerDate", value = "注册时间")
    @JsonProperty("registerDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date registerDate;

    @ApiModelProperty(name = "registerIp", value = "注册IP")
    @JsonProperty("registerIp")
    private String registerIp;

    @ApiModelProperty(name = "admin", value = "是否管理员")
    @JsonProperty("admin")
    private Boolean admin;

    @ApiModelProperty(name = "superAdmin", value = "是否系统管理员")
    @JsonProperty("superAdmin")
    private Boolean superAdmin;
}
