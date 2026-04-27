package com.github.thundax.modules.sys.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.vo.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/** @author wdit */
@ApiModel(value = "Office", description = "组织机构")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfficeVo extends BaseVo {

    private String parentId;
    private String name;
    private String shortName;
    private String namePath;

    public OfficeVo() {
        super();
    }

    public OfficeVo(String id) {
        super(id);
    }

    @ApiModelProperty(name = "parentId", value = "父节点ID")
    @JsonProperty("parentId")
    @Size(max = 64, message = "父节点ID长度不能超过64")
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @ApiModelProperty(name = "name", value = "名称")
    @JsonProperty("name")
    @NotEmpty(message = "\"名称\"不能为空")
    @Size(max = 50, message = "\"名称\"长度不能超过 50")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(name = "namePath", value = "全名称")
    @JsonProperty("namePath")
    public String getNamePath() {
        return namePath;
    }

    public void setNamePath(String namePath) {
        this.namePath = namePath;
    }

    @ApiModelProperty(name = "shortName", value = "简称")
    @JsonProperty("shortName")
    @Size(max = 50, message = "\"简称\"长度不能超过 50")
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
