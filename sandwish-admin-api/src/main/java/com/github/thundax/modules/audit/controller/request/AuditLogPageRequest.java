package com.github.thundax.modules.audit.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "AuditLogPageRequest", description = "审计日志分页请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditLogPageRequest {

    @ApiModelProperty(name = "pageNo", value = "页码")
    private Integer pageNo;

    @ApiModelProperty(name = "pageSize", value = "页大小")
    private Integer pageSize;

    @ApiModelProperty(name = "objectType", value = "对象类型")
    private String objectType;

    @ApiModelProperty(name = "objectId", value = "对象ID")
    private String objectId;

    @ApiModelProperty(name = "action", value = "审计动作")
    private String action;

    @ApiModelProperty(name = "operatorType", value = "操作者类型")
    private String operatorType;

    @ApiModelProperty(name = "operatorId", value = "操作者ID")
    private String operatorId;

    @ApiModelProperty(name = "source", value = "来源")
    private String source;

    @ApiModelProperty(name = "requestId", value = "请求ID")
    private String requestId;

    @ApiModelProperty(name = "beginDate", value = "开始时间")
    private Date beginDate;

    @ApiModelProperty(name = "endDate", value = "结束时间")
    private Date endDate;
}
