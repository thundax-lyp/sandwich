package com.github.thundax.modules.sys.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
@ApiModel(value = "UserQueryRequest", description = "用户查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserQueryRequest implements Serializable {

    @ApiModelProperty(name = "pageNo", value = "页码，从1开始", example = "1")
    @JsonProperty("pageNo")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = 1;

    @ApiModelProperty(name = "pageSize", value = "单页记录数", example = "5")
    @JsonProperty("pageSize")
    @Min(value = 1, message = "单页记录数不能小于1")
    @Max(value = 500, message = "单页记录数不能超过500")
    private Integer pageSize = 10;

    @ApiModelProperty(name = "officeId", value = "组织机构ID")
    @JsonProperty("officeId")
    @Size(max = 64, message = "\"组织机构ID\"长度不能超过64")
    private String officeId;

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
