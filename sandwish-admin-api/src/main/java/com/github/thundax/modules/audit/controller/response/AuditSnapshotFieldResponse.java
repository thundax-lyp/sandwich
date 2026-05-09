package com.github.thundax.modules.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@ApiModel(value = "AuditSnapshotFieldResponse", description = "审计快照字段响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditSnapshotFieldResponse implements Serializable {

    @ApiModelProperty(name = "fieldName", value = "字段名")
    private String fieldName;

    @ApiModelProperty(name = "fieldLabel", value = "字段标签")
    private String fieldLabel;

    @ApiModelProperty(name = "value", value = "原始值")
    private Object value;

    @ApiModelProperty(name = "displayValue", value = "展示值")
    private String displayValue;

    @ApiModelProperty(name = "valueType", value = "值类型")
    private String valueType;

    @ApiModelProperty(name = "sensitive", value = "是否敏感")
    private Boolean sensitive;
}
