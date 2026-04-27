package com.github.thundax.modules.sys.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
@ApiModel(value = "RolePriorityRequest", description = "角色排序请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RolePriorityRequest implements Serializable {

    @ApiModelProperty(name = "id", value = "角色ID")
    @JsonProperty("id")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @ApiModelProperty(name = "priority", value = "排序数", example = "0")
    @JsonProperty("priority")
    @Min(value = 0, message = "\"排序数\"必须不能小于 0")
    private Integer priority;
}
