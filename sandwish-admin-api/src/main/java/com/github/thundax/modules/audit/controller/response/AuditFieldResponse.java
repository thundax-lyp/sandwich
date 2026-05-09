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
@ApiModel(value = "AuditFieldResponse", description = "审计字段响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditFieldResponse implements Serializable {

    @ApiModelProperty(name = "fieldName", value = "字段名")
    private String fieldName;

    @ApiModelProperty(name = "fieldLabel", value = "字段标签")
    private String fieldLabel;

    @ApiModelProperty(name = "beforeDisplayValue", value = "变更前展示值")
    private String beforeDisplayValue;

    @ApiModelProperty(name = "afterDisplayValue", value = "变更后展示值")
    private String afterDisplayValue;
}
