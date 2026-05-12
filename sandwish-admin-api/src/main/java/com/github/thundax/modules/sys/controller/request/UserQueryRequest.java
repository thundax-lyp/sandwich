package com.github.thundax.modules.sys.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.web.request.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "UserQueryRequest", description = "用户查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserQueryRequest extends PageRequest {

    @ApiModelProperty(name = "departmentId", value = "部门ID")
    @JsonProperty("departmentId")
    @Size(max = 64, message = "\"部门ID\"长度不能超过64")
    private String departmentId;

    @ApiModelProperty(name = "loginName", value = "登录名，模糊查询")
    @JsonProperty("loginName")
    @Size(max = 30, message = "\"登录名\"长度不能超过 30")
    private String loginName;

    @ApiModelProperty(name = "name", value = "姓名，模糊查询")
    @JsonProperty("name")
    @Size(max = 30, message = "\"姓名\"长度不能超过 30")
    private String name;

    @ApiModelProperty(name = "enable", value = "启用/禁用")
    @JsonProperty("enable")
    private Boolean enable;

    @ApiModelProperty(name = "orderBy", value = "排序规则")
    @JsonProperty("orderBy")
    private String orderBy;
}
