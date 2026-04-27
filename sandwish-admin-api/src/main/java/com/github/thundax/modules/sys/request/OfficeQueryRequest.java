package com.github.thundax.modules.sys.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "OfficeQueryRequest", description = "机构查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfficeQueryRequest implements Serializable {

    @ApiModelProperty(name = "parentId", value = "父节点ID，为空则查询跟节点")
    @JsonProperty("parentId")
    @Size(max = 64, message = "父节点ID长度不能超过64")
    private String parentId;

    @ApiModelProperty(name = "name", value = "名称，模糊匹配名称和简称")
    @JsonProperty("name")
    @Size(max = 50, message = "名称长度不能超过50")
    private String name;

    @ApiModelProperty(name = "remarks", value = "备注，模糊匹配")
    @JsonProperty("remarks")
    @Size(max = 200, message = "名称长度不能超过200")
    private String remarks;
}
