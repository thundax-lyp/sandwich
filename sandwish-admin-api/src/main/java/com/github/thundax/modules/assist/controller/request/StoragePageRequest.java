package com.github.thundax.modules.assist.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "StoragePageRequest", description = "存储资源分页查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoragePageRequest implements Serializable {

    @ApiModelProperty(name = "pageNo", value = "页码，从1开始", example = "1")
    @JsonProperty("pageNo")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = 1;

    @ApiModelProperty(name = "pageSize", value = "单页记录数", example = "10")
    @JsonProperty("pageSize")
    @Min(value = 1, message = "单页记录数不能小于1")
    @Max(value = 500, message = "单页记录数不能超过500")
    private Integer pageSize = 10;

    @ApiModelProperty(name = "mimeType", value = "MIME-TYPE")
    @JsonProperty("mimeType")
    @Size(max = 128, message = "MIME-TYPE长度不能超过128")
    private String mimeType;

    @ApiModelProperty(name = "status", value = "状态")
    @JsonProperty("status")
    @Size(max = 32, message = "状态长度不能超过32")
    private String status;

    @ApiModelProperty(name = "visibility", value = "可见性")
    @JsonProperty("visibility")
    @Size(max = 32, message = "可见性长度不能超过32")
    private String visibility;

    @ApiModelProperty(name = "name", value = "文件名称")
    @JsonProperty("name")
    @Size(max = 255, message = "文件名称长度不能超过255")
    private String name;

    @ApiModelProperty(name = "remarks", value = "备注")
    @JsonProperty("remarks")
    @Size(max = 255, message = "备注长度不能超过255")
    private String remarks;
}
