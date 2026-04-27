package com.github.thundax.modules.sys.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "DictPageRequest", description = "字典分页查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DictPageRequest implements Serializable {

    @ApiModelProperty(name = "pageNo", value = "页码，从1开始", example = "1")
    @JsonProperty("pageNo")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = 1;

    @ApiModelProperty(name = "pageSize", value = "单页记录数", example = "5")
    @JsonProperty("pageSize")
    @Min(value = 1, message = "单页记录数不能小于1")
    @Max(value = 500, message = "单页记录数不能超过500")
    private Integer pageSize = 10;

    @ApiModelProperty(name = "label", value = "标签")
    @JsonProperty("label")
    private String label;

    @ApiModelProperty(name = "type", value = "类型")
    @JsonProperty("type")
    private String type;

    @ApiModelProperty(name = "remarks", value = "备注")
    @JsonProperty("remarks")
    private String remarks;
}
