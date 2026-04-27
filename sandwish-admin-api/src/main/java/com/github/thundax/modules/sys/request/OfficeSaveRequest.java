package com.github.thundax.modules.sys.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "OfficeSaveRequest", description = "机构保存请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfficeSaveRequest implements Serializable {

    @ApiModelProperty(name = "id", value = "机构ID")
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

    @ApiModelProperty(name = "parentId", value = "父节点ID")
    @JsonProperty("parentId")
    @Size(max = 64, message = "父节点ID长度不能超过64")
    private String parentId;

    @ApiModelProperty(name = "name", value = "名称")
    @JsonProperty("name")
    @NotEmpty(message = "\"名称\"不能为空")
    @Size(max = 50, message = "\"名称\"长度不能超过 50")
    private String name;

    @ApiModelProperty(name = "shortName", value = "简称")
    @JsonProperty("shortName")
    @Size(max = 50, message = "\"简称\"长度不能超过 50")
    private String shortName;
}
