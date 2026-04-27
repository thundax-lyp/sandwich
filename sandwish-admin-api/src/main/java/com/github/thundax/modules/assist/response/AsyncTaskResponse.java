package com.github.thundax.modules.assist.response;

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
@ApiModel(value = "AsyncTaskResponse", description = "异步任务响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsyncTaskResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "异步任务ID")
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

    @ApiModelProperty(name = "status", value = "状态")
    @JsonProperty("status")
    private String status;

    @ApiModelProperty(name = "message", value = "消息")
    @JsonProperty("message")
    private String message;

    @ApiModelProperty(name = "data", value = "数据")
    @JsonProperty("data")
    private Object data;
}
