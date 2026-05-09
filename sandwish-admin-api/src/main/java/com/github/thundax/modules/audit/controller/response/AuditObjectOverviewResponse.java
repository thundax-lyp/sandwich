package com.github.thundax.modules.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@ApiModel(value = "AuditObjectOverviewResponse", description = "对象审计概览响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditObjectOverviewResponse implements Serializable {

    @ApiModelProperty(name = "meta", value = "审计元数据")
    private AuditMetaResponse meta;

    @ApiModelProperty(name = "latestLogs", value = "最近审计日志")
    private List<AuditLogResponse> latestLogs = new ArrayList<>();
}
