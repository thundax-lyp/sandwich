package com.github.thundax.modules.sys.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "LogDepartmentResponse", description = "日志部门摘要响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogDepartmentResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "部门ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "name", value = "名称")
    @JsonProperty("name")
    private String name;

    @ApiModelProperty(name = "namePath", value = "全名称")
    @JsonProperty("namePath")
    private String namePath;
}
