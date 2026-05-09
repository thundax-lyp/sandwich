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
@ApiModel(value = "MultipartUploadPartResponse", description = "分片上传分片响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultipartUploadPartResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "分片ID")
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty(name = "uploadId", value = "上传会话标识")
    @JsonProperty("uploadId")
    private String uploadId;

    @ApiModelProperty(name = "partNumber", value = "分片序号")
    @JsonProperty("partNumber")
    private Integer partNumber;

    @ApiModelProperty(name = "etag", value = "分片ETag")
    @JsonProperty("etag")
    private String etag;

    @ApiModelProperty(name = "size", value = "分片大小")
    @JsonProperty("size")
    private Long size;
}
