package com.github.thundax.modules.submission.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "SubmissionStatusRequest", description = "提交内容状态请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmissionStatusRequest implements Serializable {

    @ApiModelProperty(name = "id", value = "提交内容ID")
    @JsonProperty("id")
    @NotEmpty(message = "ID不能为空")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @ApiModelProperty(name = "status", value = "提交状态")
    @JsonProperty("status")
    @NotEmpty(message = "提交状态不能为空")
    @Size(max = 32, message = "提交状态长度不能超过32")
    private String status;
}
