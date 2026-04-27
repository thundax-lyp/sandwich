package com.github.thundax.modules.sys.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ApiModel(value = "MenuResponse", description = "菜单响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "菜单ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "priority", value = "排序数")
    @JsonProperty("priority")
    private Integer priority;

    @ApiModelProperty(name = "remarks", value = "备注")
    @JsonProperty("remarks")
    private String remarks;

    @ApiModelProperty(name = "createDate", value = "创建时间")
    @JsonProperty("createDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

    @ApiModelProperty(name = "updateDate", value = "修改时间")
    @JsonProperty("updateDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateDate;

    @ApiModelProperty(name = "parentId", value = "父节点ID")
    @JsonProperty("parentId")
    private String parentId;

    @ApiModelProperty(name = "name", value = "名称")
    @JsonProperty("name")
    private String name;

    @ApiModelProperty(name = "perms", value = "权限")
    @JsonProperty("perms")
    private String perms;

    @ApiModelProperty(name = "ranks", value = "等级")
    @JsonProperty("ranks")
    private Integer ranks;

    @ApiModelProperty(name = "display", value = "显示/隐藏")
    @JsonProperty("display")
    private Boolean display;

    @ApiModelProperty(name = "displayParams", value = "显示参数，前端使用")
    @JsonProperty("displayParams")
    private String displayParams;

    @ApiModelProperty(name = "url", value = "URL")
    @JsonProperty("url")
    private String url;
}
