package com.github.thundax.modules.submission.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.web.request.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "SubmissionPageRequest", description = "提交内容分页查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmissionPageRequest extends PageRequest {

    @ApiModelProperty(name = "status", value = "提交状态")
    @JsonProperty("status")
    private String status;

    @ApiModelProperty(name = "submittedAtBegin", value = "提交开始时间")
    @JsonProperty("submittedAtBegin")
    private Date submittedAtBegin;

    @ApiModelProperty(name = "submittedAtEnd", value = "提交结束时间")
    @JsonProperty("submittedAtEnd")
    private Date submittedAtEnd;

    @ApiModelProperty(name = "sortDirection", value = "排序方向")
    @JsonProperty("sortDirection")
    private SortDirection sortDirection = SortDirection.ASC;
}
