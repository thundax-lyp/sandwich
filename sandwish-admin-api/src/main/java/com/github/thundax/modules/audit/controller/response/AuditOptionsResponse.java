package com.github.thundax.modules.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "AuditOptionsResponse", description = "审计筛选选项响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditOptionsResponse implements Serializable {

    @ApiModelProperty(name = "objectTypes", value = "对象类型")
    private List<AuditOptionResponse> objectTypes = new ArrayList<>();

    @ApiModelProperty(name = "actions", value = "审计动作")
    private List<AuditOptionResponse> actions = new ArrayList<>();

    @ApiModelProperty(name = "operatorTypes", value = "操作者类型")
    private List<AuditOptionResponse> operatorTypes = new ArrayList<>();
}
