package com.github.thundax.modules.sys.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.vo.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * @author wdit
 */
@ApiModel(value = "Menu", description = "菜单")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuVo extends BaseVo {

    private String parentId;
    private String name;
    private String perms;
    private Integer ranks;
    private String displayParams;
    private Boolean isDisplay;
    private String url;

    public MenuVo() {
        super();
    }

    public MenuVo(String id) {
        super(id);
    }


    @ApiModelProperty(name = "parentId", value = "父节点ID")
    @JsonProperty("parentId")
    @Size(max = 64, message = "父节点ID长度必须小于64")
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


    @ApiModelProperty(name = "perms", value = "权限")
    @JsonProperty("perms")
    public String getPerms() {
        return perms;
    }

    public void setPerms(String perms) {
        this.perms = perms;
    }


    @ApiModelProperty(name = "ranks", value = "等级", example = "0")
    @JsonProperty("ranks")
    @Max(value = 9, message = "\"等级\"不能大于 9")
    public Integer getRanks() {
        return ranks;
    }

    public void setRanks(Integer ranks) {
        this.ranks = ranks;
    }


    @ApiModelProperty(name = "display", value = "显示/隐藏")
    @JsonProperty("display")
    public Boolean getDisplay() {
        return isDisplay;
    }

    public void setDisplay(Boolean display) {
        isDisplay = display;
    }


    @ApiModelProperty(name = "displayParams", value = "显示参数，前端使用")
    @JsonProperty("displayParams")
    @Size(max = 1000, message = "\"显示参数\"长度不能超过 1000")
    public String getDisplayParams() {
        return displayParams;
    }

    public void setDisplayParams(String displayParams) {
        this.displayParams = displayParams;
    }


    @ApiModelProperty(name = "url", value = "URL")
    @JsonProperty("url")
    @Size(max = 200, message = "\"URL\"长度不能超过 200")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
