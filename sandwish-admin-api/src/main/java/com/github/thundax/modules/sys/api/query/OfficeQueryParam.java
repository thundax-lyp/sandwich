package com.github.thundax.modules.sys.api.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.Size;

@ApiModel(value = "OfficeQueryParam", description = "组织机构查询参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfficeQueryParam implements Serializable {

    private String parentId;
    private String name;
    private String remarks;

    @ApiModelProperty(name = "parentId", value = "父节点ID，为空则查询跟节点")
    @JsonProperty("parentId")
    @Size(max = 64, message = "父节点ID长度不能超过64")
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @ApiModelProperty(name = "name", value = "名称，模糊匹配名称和简称")
    @JsonProperty("name")
    @Size(max = 50, message = "名称长度不能超过50")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(name = "remarks", value = "备注，模糊匹配")
    @JsonProperty("remarks")
    @Size(max = 200, message = "名称长度不能超过200")
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
