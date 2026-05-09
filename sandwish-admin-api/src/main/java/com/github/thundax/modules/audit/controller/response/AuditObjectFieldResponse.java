package com.github.thundax.modules.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(value = "AuditObjectFieldResponse", description = "审计对象字段响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditObjectFieldResponse implements Serializable {

    @ApiModelProperty(name = "fieldName", value = "字段名")
    private String fieldName;

    @ApiModelProperty(name = "fieldLabel", value = "字段标签")
    private String fieldLabel;
}
