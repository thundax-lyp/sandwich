package com.github.thundax.modules.storage.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.web.request.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
@ApiModel(value = "StoragePageRequest", description = "存储资源分页查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoragePageRequest extends PageRequest {

    @ApiModelProperty(name = "contentType", value = "MIME-TYPE")
    @JsonProperty("contentType")
    @Size(max = 128, message = "MIME-TYPE长度不能超过128")
    private String contentType;

    @ApiModelProperty(name = "objectStatus", value = "状态")
    @JsonProperty("objectStatus")
    @Size(max = 32, message = "状态长度不能超过32")
    private String objectStatus;

    @ApiModelProperty(name = "referenceStatus", value = "引用状态")
    @JsonProperty("referenceStatus")
    @Size(max = 32, message = "引用状态长度不能超过32")
    private String referenceStatus;

    @ApiModelProperty(name = "originalFilename", value = "文件名称")
    @JsonProperty("originalFilename")
    @Size(max = 255, message = "文件名称长度不能超过255")
    private String originalFilename;

    @ApiModelProperty(name = "remarks", value = "备注")
    @JsonProperty("remarks")
    @Size(max = 255, message = "备注长度不能超过255")
    private String remarks;
}
