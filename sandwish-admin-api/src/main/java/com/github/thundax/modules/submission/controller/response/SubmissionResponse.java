package com.github.thundax.modules.submission.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(value = "SubmissionResponse", description = "提交内容响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmissionResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "提交内容ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "title", value = "标题")
    @JsonProperty("title")
    private String title;

    @ApiModelProperty(name = "content", value = "正文")
    @JsonProperty("content")
    private String content;

    @ApiModelProperty(name = "status", value = "提交状态")
    @JsonProperty("status")
    private String status;

    @ApiModelProperty(name = "submittedAt", value = "提交时间")
    @JsonProperty("submittedAt")
    private Date submittedAt;

    @ApiModelProperty(name = "imageObjectIds", value = "图片对象ID列表")
    @JsonProperty("imageObjectIds")
    private List<String> imageObjectIds;
}
