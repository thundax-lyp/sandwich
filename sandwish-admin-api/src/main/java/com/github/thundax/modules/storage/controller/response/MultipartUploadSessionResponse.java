package com.github.thundax.modules.storage.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(value = "MultipartUploadSessionResponse", description = "分片上传会话响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultipartUploadSessionResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "会话ID")
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty(name = "uploadId", value = "上传会话标识")
    @JsonProperty("uploadId")
    private String uploadId;

    @ApiModelProperty(name = "uploadStatus", value = "上传状态")
    @JsonProperty("uploadStatus")
    private String uploadStatus;

    @ApiModelProperty(name = "uploadedPartCount", value = "已上传分片数")
    @JsonProperty("uploadedPartCount")
    private Integer uploadedPartCount;
}
