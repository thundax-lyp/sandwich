package com.github.thundax.modules.assist.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "StorageResponse", description = "存储资源响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "存储资源ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "originalFilename", value = "文件名称")
    @JsonProperty("originalFilename")
    private String originalFilename;

    @ApiModelProperty(name = "extendName", value = "扩展名")
    @JsonProperty("extendName")
    private String extendName;

    @ApiModelProperty(name = "contentType", value = "MIME-TYPE")
    @JsonProperty("contentType")
    private String contentType;

    @ApiModelProperty(name = "ownerId", value = "所有者ID")
    @JsonProperty("ownerId")
    private String ownerId;

    @ApiModelProperty(name = "ownerType", value = "所有者类型")
    @JsonProperty("ownerType")
    private String ownerType;

    @ApiModelProperty(name = "objectStatus", value = "状态")
    @JsonProperty("objectStatus")
    private String objectStatus;

    @ApiModelProperty(name = "referenceStatus", value = "可见性")
    @JsonProperty("referenceStatus")
    private String referenceStatus;

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

    @ApiModelProperty(name = "contentUrl", value = "预览URL")
    @JsonProperty("contentUrl")
    private String contentUrl;
}
