package com.github.thundax.modules.sys.api.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.Size;

/** @author wdit */
@ApiModel(value = "MenuQueryParam", description = "菜单查询参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuQueryParam implements Serializable {

    private String parentId;
    private Boolean display;

    @ApiModelProperty(name = "parentId", value = "父节点ID，为\"ROOT\"则查询跟节点")
    @JsonProperty("parentId")
    @Size(max = 64, message = "\"父节点ID\"长度不能超过 64")
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @ApiModelProperty(name = "display", value = "显示/隐藏")
    @JsonProperty("display")
    public Boolean getDisplay() {
        return display;
    }

    public void setDisplay(Boolean display) {
        this.display = display;
    }
}
