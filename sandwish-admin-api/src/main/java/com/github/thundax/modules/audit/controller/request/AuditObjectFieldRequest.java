package com.github.thundax.modules.audit.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "AuditObjectFieldRequest", description = "审计对象字段请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditObjectFieldRequest implements Serializable {

    @NotBlank
    @ApiModelProperty(name = "objectType", value = "对象类型")
    private String objectType;
}
