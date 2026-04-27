package com.github.thundax.modules.sys.request;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import java.util.Date;

@Getter
@Setter
@ApiModel(value = "LogPageRequest", description = "日志分页查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogPageRequest implements Serializable {

    @ApiModelProperty(name = "pageNo", value = "页码，从1开始", example = "1")
    @JsonProperty("pageNo")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = 1;

    @ApiModelProperty(name = "pageSize", value = "单页记录数", example = "5")
    @JsonProperty("pageSize")
    @Min(value = 1, message = "单页记录数不能小于1")
    @Max(value = 500, message = "单页记录数不能超过500")
    private Integer pageSize = 10;

    @ApiModelProperty(name = "title", value = "标题")
    @JsonProperty("title")
    @Size(max = 50, message = "\"标题\"长度不能超过 50")
    private String title;

    @ApiModelProperty(name = "userLoginName", value = "用户登录名")
    @JsonProperty("userLoginName")
    @Size(max = 50, message = "\"用户登录名\"长度不能超过 50")
    private String userLoginName;

    @ApiModelProperty(name = "userName", value = "用户名")
    @JsonProperty("userName")
    @Size(max = 50, message = "\"用户名\"长度不能超过 50")
    private String userName;

    @ApiModelProperty(name = "remoteAddr", value = "来源")
    @JsonProperty("remoteAddr")
    @Size(max = 50, message = "\"来源\"长度不能超过 50")
    private String remoteAddr;

    @ApiModelProperty(name = "requestUri", value = "请求地址")
    @JsonProperty("requestUri")
    @Size(max = 500, message = "\"请求地址\"长度不能超过 500")
    private String requestUri;

    @ApiModelProperty(name = "beginDate", value = "开始时间，格式: yyyy-MM-dd HH:mm:ss")
    @JsonProperty("beginDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginDate;

    @ApiModelProperty(name = "endDate", value = "结束时间，格式: yyyy-MM-dd HH:mm:ss")
    @JsonProperty("endDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDate;
}
