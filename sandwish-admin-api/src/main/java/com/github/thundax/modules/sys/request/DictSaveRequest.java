package com.github.thundax.modules.sys.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "DictSaveRequest", description = "字典保存请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DictSaveRequest implements Serializable {

    @ApiModelProperty(name = "id", value = "字典ID")
    @JsonProperty("id")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @ApiModelProperty(name = "priority", value = "排序数", example = "0")
    @JsonProperty("priority")
    @Min(value = 0, message = "\"排序数\"必须不能小于 0")
    private Integer priority;

    @ApiModelProperty(name = "remarks", value = "备注")
    @JsonProperty("remarks")
    @Size(max = 200, message = "\"备注\"长度不能超过 200")
    private String remarks;

    @ApiModelProperty(name = "type", value = "类型")
    @JsonProperty("type")
    private String type;

    @ApiModelProperty(name = "label", value = "标签")
    @JsonProperty("label")
    private String label;

    @ApiModelProperty(name = "value", value = "值")
    @JsonProperty("value")
    private String value;
}
