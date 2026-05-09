package com.github.thundax.modules.sys.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.web.request.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "DictPageRequest", description = "字典分页查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DictPageRequest extends PageRequest {

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
