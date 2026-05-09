package com.github.thundax.modules.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(value = "AuditLogResponse", description = "审计日志响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditLogResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "ID")
    private Long id;

    @ApiModelProperty(name = "objectType", value = "对象类型")
    private String objectType;

    @ApiModelProperty(name = "objectId", value = "对象ID")
    private String objectId;

    @ApiModelProperty(name = "objectDisplayName", value = "对象展示名")
    private String objectDisplayName;

    @ApiModelProperty(name = "objectTypeLabel", value = "对象类型标签")
    private String objectTypeLabel;

    @ApiModelProperty(name = "version", value = "审计版本")
    private Long version;

    @ApiModelProperty(name = "action", value = "动作")
    private String action;

    @ApiModelProperty(name = "actionLabel", value = "动作标签")
    private String actionLabel;

    @ApiModelProperty(name = "operatorType", value = "操作者类型")
    private String operatorType;

    @ApiModelProperty(name = "operatorTypeLabel", value = "操作者类型标签")
    private String operatorTypeLabel;

    @ApiModelProperty(name = "operatorId", value = "操作者ID")
    private String operatorId;

    @ApiModelProperty(name = "operatorName", value = "操作者")
    private String operatorName;

    @ApiModelProperty(name = "source", value = "来源")
    private String source;

    @ApiModelProperty(name = "requestId", value = "请求ID")
    private String requestId;

    @ApiModelProperty(name = "traceId", value = "链路ID")
    private String traceId;

    @ApiModelProperty(name = "remoteAddr", value = "远端地址")
    private String remoteAddr;

    @ApiModelProperty(name = "summary", value = "摘要")
    private String summary;

    @ApiModelProperty(name = "occurredAt", value = "发生时间")
    private Date occurredAt;

    @ApiModelProperty(name = "changedFields", value = "变更字段")
    private List<AuditFieldResponse> changedFields = new ArrayList<>();

    @ApiModelProperty(name = "changedFieldCount", value = "变更字段数")
    private Integer changedFieldCount;
}
