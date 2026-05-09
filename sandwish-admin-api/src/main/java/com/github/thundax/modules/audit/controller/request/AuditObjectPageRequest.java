package com.github.thundax.modules.audit.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "AuditObjectPageRequest", description = "对象审计分页请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditObjectPageRequest extends AuditMetaRequest {

    @ApiModelProperty(name = "pageNo", value = "页码")
    private Integer pageNo;

    @ApiModelProperty(name = "pageSize", value = "页大小")
    private Integer pageSize;
}
