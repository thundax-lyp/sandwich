package com.github.thundax.modules.audit.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.thundax.common.web.request.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "AuditObjectPageRequest", description = "对象审计分页请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditObjectPageRequest extends PageRequest {

    @NotBlank
    @ApiModelProperty(name = "objectType", value = "对象类型")
    private String objectType;

    @NotBlank
    @ApiModelProperty(name = "objectId", value = "对象ID")
    private String objectId;
}
