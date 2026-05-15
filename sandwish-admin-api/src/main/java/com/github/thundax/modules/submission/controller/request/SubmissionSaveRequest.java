package com.github.thundax.modules.submission.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "SubmissionSaveRequest", description = "提交内容保存请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmissionSaveRequest implements Serializable {

    @ApiModelProperty(name = "title", value = "标题")
    @JsonProperty("title")
    @NotEmpty(message = "\"标题\"不能为空")
    @Size(max = 200, message = "\"标题\"长度不能超过 200")
    private String title;

    @ApiModelProperty(name = "content", value = "正文")
    @JsonProperty("content")
    @NotEmpty(message = "\"正文\"不能为空")
    private String content;

    @ApiModelProperty(name = "imageObjectIds", value = "图片对象ID列表")
    @JsonProperty("imageObjectIds")
    private List<String> imageObjectIds;
}
