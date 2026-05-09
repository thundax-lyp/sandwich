package com.github.thundax.modules.audit.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Getter
@Setter
@ApiModel(value = "AuditMetaRequest", description = "审计元数据请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditMetaRequest implements Serializable {

    @NotBlank
    @ApiModelProperty(name = "objectType", value = "对象类型")
    private String objectType;

    @NotBlank
    @ApiModelProperty(name = "objectId", value = "对象ID")
    private String objectId;
}
